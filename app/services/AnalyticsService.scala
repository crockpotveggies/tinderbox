package services

import akka.actor.{Props, Actor}
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent._
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import org.mapdb._
import utils.tinder.TinderApi
import utils.tinder.model._
import models.{MatchUpdate, Notification}
import java.util.concurrent.ConcurrentNavigableMap
import java.util.{TimeZone, Date}
import java.text.{SimpleDateFormat, DateFormat}

/**
 * Stores analysis of messages and swipes.
 */
object AnalyticsService {
  // if we don't set the ClassLoader it will be stuck in SBT
  Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

  /**
   * History and notification storage
   */
  private val messageSentiments: ConcurrentNavigableMap[String, Map[String, String]] = MapDB.db.getTreeMap("message_sentiments")

  /**
   * Actor for performing batch processing of message sentiments.
   */
  private class SentimentsTask extends Actor {
    def receive = {
      case "tick" =>
        TinderService.activeSessions.foreach { s => syncSentiments(s) }
        Logger.debug("[mapdb] Database committer has persisted data to disk.")
    }
  }
  private val sentimentActor = Akka.system.actorOf(Props[SentimentsTask], name = "SentimentsTask")
  private val sentimentService = {
    Akka.system.scheduler.schedule(0 seconds, 3600 seconds, sentimentActor, "tick")
  }

  /**
   * Performs a full update of the sentiment cache.
   *
   * @param xAuthToken
   */
  private def syncSentiments(xAuthToken: String): Map[String, String] = {
    val session = TinderService.fetchSession(xAuthToken).get
    UpdatesService.fetchHistory(xAuthToken) match {
      case None => Map()
      case Some(history) =>
        val data = history
          .filterNot { m => m.person==None }
          .map { m =>
            val senderMessages = m.messages.filterNot( m => m.from==session.user._id )
            (m, senderMessages.map(_.message).mkString(" "))
          }
          .map{ m =>
            val sentiment = models.bot.tasks.message.MessageSentiment.findSentiment(m._2)
            Logger.debug("[analytics] Sentiment for match with %s was %s".format(m._1.person.get.name, sentiment.toString))
            (m._1._id, sentiment.toString)
          }
          .toMap

        messageSentiments.put(session.user._id, data)
        data
    }
  }

  /**
   * Retrieves latest sentiment analysis.
   * @param xAuthToken
   * @return
   */
  def fetchSentiments(xAuthToken: String): Option[Map[String, String]] = {
    val session = TinderService.fetchSession(xAuthToken).get
    messageSentiments.get(session.user._id) match {
      case null =>
        Some(syncSentiments(xAuthToken))
      case sentiments =>
        Some(sentiments)
    }
  }

}
