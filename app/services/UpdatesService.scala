package services

import akka.actor.{Props, Actor}
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.collection.mutable.Map
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import scala.collection.JavaConversions._
import org.mapdb._
import utils.tinder.TinderApi
import utils.tinder.model._
import utils.TimeUtil
import models.{MatchUpdate, Notification}
import java.util.concurrent.ConcurrentNavigableMap
import java.util.{TimeZone, Date}
import java.text.{SimpleDateFormat, DateFormat}

/**
 * A service that pulls, caches, and notifies of updates.
 */
object UpdatesService {
  // if we don't set the ClassLoader it will be stuck in SBT
  Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

  /*
   * History and notification storage
   */
  private val matches: ConcurrentNavigableMap[String, List[Match]] = MapDB.db.getTreeMap("matches")

  private val notifications: ConcurrentNavigableMap[String, List[Notification]] = MapDB.db.getTreeMap("notifications")

  private val unreadCounts: ConcurrentNavigableMap[String, Map[String, Int]] = MapDB.db.getTreeMap("unread_counts")

  // Required for timing updates properly
  private val lastActivity: ConcurrentNavigableMap[String, Date] = MapDB.db.getTreeMap("last_activity")

  // Used for storing stop-gaps to prevent further bot messaging
  private val match_stopgaps: ConcurrentNavigableMap[String, List[String]] = MapDB.db.getTreeMap("match_stopgaps")

  /**
   * Actor for performing batch processing of message sentiments.
   */
  private class UpdatesTask extends Actor {
    def receive = {
      case "tick" =>
        TinderService.activeSessions.foreach { s => syncUpdates(s) }
        Logger.debug("[mapdb] Database committer has persisted data to disk.")
    }
  }
  private val updateActor = Akka.system.actorOf(Props[UpdatesTask], name = "UpdatesTask")
  private val updateService = {
    Akka.system.scheduler.schedule(0 seconds, 40 seconds, updateActor, "tick")
  }


  /**
   * Performs a full update of the history cache.
   *
   * Note: this resets unread counts to zero since we let updates handle that for us.
   * @param xAuthToken
   */
  private def syncHistory(xAuthToken: String): Option[List[Match]] = {
    val tinderApi = new TinderApi(Some(xAuthToken))
    val result = Await.result(tinderApi.getHistory, 30 seconds)
    result match {
      case Left(error) =>
        Logger.error("An error occurred retrieving full history for %s.".format(xAuthToken))
        Logger.error("Reason: " + error.error)
        None
      case Right(history) =>
        // update the last activity variable
        history.last_activity_date.map{ date => lastActivity.put(xAuthToken, tinderApi.ISO8601.parse(date)) }
        val data = history.matches
        if(data.size>0) matches.put(xAuthToken, data)
        Some(data)
    }
  }

  /**
   * Grabs recent updates and syncs them with history.
   * @param xAuthToken
   * @return
   */
  private def syncUpdates(xAuthToken: String): Option[(Map[String, List[Message]], List[Notification], Map[String, Int])] = {
    // just in case...
    Thread.currentThread().setContextClassLoader(play.api.Play.classloader)
    Logger.debug("[updates] Fetching updates from Tinder API")

    // grab updates and process them accordingly
    val tinderApi = new TinderApi(Some(xAuthToken))
    val result = Await.result(tinderApi.getUpdates(fetchLastActivity(xAuthToken).getOrElse(new Date())), 40 seconds)
    val session = TinderService.fetchSession(xAuthToken).get
    result match {
      case Left(error) =>
        Logger.error("An error occurred retrieving full history for %s.".format(xAuthToken))
        Logger.error("Reason: " + error.error)
        None
      case Right(history) =>
        // check if blocks need to be removed
        history.blocks.foreach { blockId =>
          deleteMessages(xAuthToken, blockId)
        }
        val messages = Map(history.matches.map( m => (m._id, m.messages)).toMap.toSeq: _*)
        // first update match history
        history.matches.foreach { m =>
          putMessages(xAuthToken, m._id, m.messages)
        }
        history.last_activity_date.map{ date => lastActivity.put(xAuthToken, tinderApi.ISO8601.parse(date)) }
        // then create notifications
        val notificationList = history.matches.map { m =>
          // make sure we only notify based on sender's messages
          val fromMessages = m
            .messages
            .filterNot( x => x.from==session.user._id )
          new Notification(
            "messages",
            m._id,
            m.person.map(_.name).getOrElse("Someone"),
            "New messages from %s.".format(m.person.map(_.name).getOrElse("a Tinderer")),
            fromMessages.size
          )
        }
        notifications.put(xAuthToken, notificationList)
        // finally append new notifications to existing read counts
        val unreads = Option(unreadCounts.get(xAuthToken)) match {
          case None =>
            Map(notificationList.map { n =>
              (n.associateId, n.size)
            }.toMap.toSeq: _*)
          case Some(counts) =>
            counts.map { n =>
              val countAppend = notificationList.filter(o => o.associateId==n._1).headOption match {
                case None => 0
                case Some(o) => o.size
              }
              (n._1, countAppend + n._2)
            }
        }
        unreadCounts.put(xAuthToken, unreads)

        if(history.matches.size>0) Logger.info("[updates] Retrieved %s updates from Tinder API" format history.matches.size)
        Some((messages, notificationList, unreads))
    }
  }

  /**
   * Force an update of message history.
   * @param xAuthToken
   */
  def forceHistorySync(xAuthToken: String) {
    matches.remove(xAuthToken)
    syncHistory(xAuthToken)
  }

  /**
   * Updates a list of messages into match history.
   * @param xAuthToken
   * @param matchId
   * @param messages
   */
  def putMessages(xAuthToken: String, matchId: String, messages: List[Message]) {
    fetchHistory(xAuthToken) match {
      case None => // do nothing, might not exist
      case Some(history) =>
        history.filter(m => m._id==matchId).headOption match {
          case None => // the match isn't in history, there are bigger problems
          case Some(m) =>
            val newMatch = m.copy(messages = messages)
            val matchList = history.filterNot(m => m._id==matchId) ::: List(newMatch)
            matches.put(xAuthToken, matchList)
        }
    }
  }

  /**
   * Removes a match from conversation.
   * @param xAuthToken
   * @param matchId
   */
  def deleteMessages(xAuthToken: String, matchId: String) {
    fetchHistory(xAuthToken) match {
      case None => // do nothing, might not exist
      case Some(history) =>
        val matchList = history.filterNot(m => m._id==matchId)
        matches.put(xAuthToken, matchList)
    }
  }

  /**
   * Retrieves full match history and all users.
   * @param xAuthToken
   * @return
   */
  def fetchHistory(xAuthToken: String): Option[List[Match]] = {
    Option(matches.get(xAuthToken)) match {
      case None =>
        syncHistory(xAuthToken)
      case Some(matches) =>
        Some(matches)
    }
  }

  /**
   * Retrieve the last date an update was valid.
   * @param xAuthToken
   * @return
   */
  def fetchLastActivity(xAuthToken: String): Option[Date] =
    Option(lastActivity.get(xAuthToken))

  /**
   * Get a list of raw updates for a user.
   * @param xAuthToken
   * @return
   */
  def fetchUpdates(xAuthToken: String): (Option[List[Notification]], Option[Map[String, Int]]) = {
    (fetchNotifications(xAuthToken), fetchUnreadCounts(xAuthToken))
  }

  /**
   * Fetches a specific match from updates history.
   * @param xAuthToken
   * @param matchId
   */
  def fetchMatch(xAuthToken: String, matchId: String): Option[Match] = {
    fetchHistory(xAuthToken) match {
      case None => None
      case Some(list) => list.find( m => m._id==matchId )
    }
  }

  /**
   * Adds a new notification.
   * @param xAuthToken
   * @param n
   */
  def appendNotification(xAuthToken: String, n: Notification) = {
    fetchNotifications(xAuthToken) match {
      case None => notifications.put(xAuthToken, List(n))
      case Some(list) =>
        notifications.put(xAuthToken, list ::: List(n))
    }
  }

  /**
   * Get a list of notifications for a user.
   * @param xAuthToken
   * @return
   */
  def fetchNotifications(xAuthToken: String): Option[List[Notification]] =
    Option(notifications.get(xAuthToken))

  /**
   * Asynchronously clears all notifications for a user.
   * @param xAuthToken
   */
  def clearNotifications(xAuthToken: String) {
    notifications.put(xAuthToken, List[Notification]())
  }

  /**
   * Grabs a list of unread counts for messages.
   * @param xAuthToken
   * @return
   */
  def fetchUnreadCounts(xAuthToken: String): Option[Map[String, Int]] =
    Option(unreadCounts.get(xAuthToken))

  /**
   * Asynchronously clears unread count for a specific match ID.
   * @param xAuthToken
   * @param matchId
   */
  def clearUnreadCount(xAuthToken: String, matchId: String) {
    fetchUnreadCounts(xAuthToken) match {
      case None => // do nothing
      case Some(counts) =>
        counts.put(matchId, 0)
        unreadCounts.put(xAuthToken, counts)
    }
  }

  /**
   * Checks if a stop-gap has been put in place for a match.
   * @param userId
   * @param matchId
   */
  def hasStopGap(userId: String, matchId: String): Boolean = {
    Option(match_stopgaps.get(userId)) match {
      case None => false
      case Some(stopgaps) =>
        stopgaps.find( o => o==matchId ) match {
          case None => false
          case Some(o) => true
        }
    }
  }

  /**
   * Creates a stop-gap for a match to prevent further bot messaging.
   * @param userId
   * @param matchId
   */
  def createStopGap(userId: String, matchId: String): Unit = {
    if(!hasStopGap(userId, matchId)) {
      Option(match_stopgaps.get(userId)) match {
        case None => match_stopgaps.put(userId, List(matchId))
        case Some(stopgaps) => match_stopgaps.put(userId, stopgaps ::: List(matchId))
      }
    }
  }

}
