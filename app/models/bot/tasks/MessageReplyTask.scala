package models.bot.tasks

import akka.actor._
import models.Notification
import models.bot.tasks.message.{FunMessages, MessageTree, MessageUtil}
import play.api.Logger
import scala.util.Random
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits._
import services._
import models.bot.BotLog
import utils.tinder.TinderApi
import utils.tinder.model._


/**
 * This task is specifically meant for opening conversations with zero messages.
 */
class MessageReplyTask(val xAuthToken: String, val tinderBot: ActorRef, val matchId: String, val userId: String) extends TaskActor {

  override def preStart() = {
    Logger.debug("[tinderbot] Starting message intro task.")
    self ! "tick"
  }

  def createStopGap(m: Match, notify: Boolean) = {
    // create a stop-gap to prevent future processing
    UpdatesService.createStopGap(userId, m._id)
    Logger.debug("[tinderbot] Created stop-gap for conversation with %s." format m.person.map(_.name).getOrElse(m._id))

    if(notify) {
      val notification = new Notification(
        "messages",
        m._id,
        m.person.map(_.name).getOrElse("Someone"),
        "A conversation with %s requires your input.".format(m.person.map(_.name).getOrElse("a Tinderer")),
        1
      )
      UpdatesService.appendNotification(xAuthToken, notification)
    }
  }

  def receive = {
    case "tick" =>
      // double-check that message data is current
      UpdatesService.forceHistorySync(xAuthToken)

      // retrieve the match
      val m = UpdatesService.fetchMatch(xAuthToken, matchId).get

      // double-check a reply hasn't been made recently
      if(!MessageUtil.checkIfReplied(userId, m.messages)) {
        // retrieve the tree used to start the conversation
        MessageUtil.extractIntroMessage(userId, m.messages) match {
          // the conversation is empty, send an opener
          case None =>
            if(m.messages.size==0) {
              val randomOpener = FunMessages.messages(Random.nextInt(FunMessages.messages.size)).value.replace("{name}", m.person.map(_.name).getOrElse(""))
              new TinderApi(Some(xAuthToken)).sendMessage(m._id, randomOpener).map { result =>
                result match {
                  case Left(e) =>
                    Logger.error("[tinderbot] Message Reply task couldn't send a message to %s: %s" format(m._id, e.error))

                  case Right(message) =>
                    val user = TinderService.fetchSession(xAuthToken).get
                    val log = BotLog(
                      System.currentTimeMillis(),
                      "message_reply",
                      "Sent message reply to %s.".format(m.person.map(_.name).getOrElse("a user")),
                      m.person.map(_._id),
                      Some(m.person.get.photos.head.url)
                    )
                    TinderBot.writeLog(user.user._id, log)
                    Logger.info("[tinderbot] Sent a message reply to %s. " format m._id)
                    Logger.debug("[tinderbot] Message reply was: \"%s...\"" format randomOpener.substring(0, 10))
                }
              }
            } else {
              createStopGap(m, true)
            }

          // there is a distinct conversation tree
          case Some(treeRoot) =>
            FunMessages.messages.find(_.value == treeRoot) match {
              case None =>
                createStopGap(m, true)

              case Some(tree) =>
                val sentiments = MessageUtil.assignSentimentDirection(MessageUtil.filterSenderMessages(userId, m.messages)).map(_._2)
                Logger.debug("[tinderbot] Sentiment directions are %s." format sentiments)

                MessageTree.walkTree(tree, sentiments) match {
                  // couldn't walk the tree to find a reply
                  case None =>
                    createStopGap(m, true)

                  // reply to the conversation with the next branch in the tree
                  case Some(branch) =>
                    new TinderApi(Some(xAuthToken)).sendMessage(m._id, branch.value).map { result =>
                      result match {
                        case Left(e) =>
                          Logger.error("[tinderbot] Message Reply task couldn't send a message to %s: %s" format(m._id, e.error))

                        case Right(message) =>
                          val user = TinderService.fetchSession(xAuthToken).get
                          val log = BotLog(
                            System.currentTimeMillis(),
                            "message_reply",
                            "Sent message reply to %s.".format(m.person.map(_.name).getOrElse("a user")),
                            m.person.map(_._id),
                            Some(m.person.get.photos.head.url)
                          )
                          TinderBot.writeLog(user.user._id, log)
                          Logger.info("[tinderbot] Sent a message reply to %s. " format m._id)
                          Logger.debug("[tinderbot] Message reply was: \"%s...\"" format branch.value.substring(0, 10))
                      }
                    }
                }

            }
        }
      }

      // make sure we properly shut down this actor
      self ! PoisonPill

    // someone is sending invalid messages
    case e: Any =>
      Logger.error("[tinderbot] Task received an unknown message")
      Logger.error("[tinderbot] Received: \n %s" format e.toString)

  }

}
