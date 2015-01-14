package models.bot.tasks

import akka.actor._
import models.bot.tasks.message.MessageUtil
import play.api.Logger
import play.api.Play.current
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits._
import services._
import utils.tinder.TinderApi
import utils.tinder.model._


/**
 * Task that creates messages based on criteria of conversation.
 */
class MessageAnalysisTask(val xAuthToken: String, val tinderBot: ActorRef) extends TaskActor {

  override def preStart() = {
    Logger.debug("[tinderbot] Starting new message analysis task.")
    self ! "tick"
  }

  def receive = {
    case "tick" =>
      // grab message history
      UpdatesService.fetchHistory(xAuthToken) match {
        case None =>
          Logger.debug("[tinderbot] Message history was empty.")

        case Some(matches) =>
          // just in case
          Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

          val session = TinderService.fetchSession(xAuthToken).get

          // filter conversations without a reply and create analysis task
          matches
            .filterNot( m => MessageUtil.checkIfReplied(session.user._id, m.messages) )
            .filterNot( m => UpdatesService.hasStopGap(session.user._id, m._id) )
            .foreach { m =>
              Logger.debug("[tinderbot] Creating new message intro task for match %s" format m._id)
              val task = Props(new MessageReplyTask(xAuthToken, tinderBot, m._id, session.user._id))
              tinderBot ! task
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
