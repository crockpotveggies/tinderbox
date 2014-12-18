package models.bot.tasks

import akka.actor._
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
    Logger.debug("[tinderbot] Starting new auto message task.")
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
          // make sure updates have first been synced
          UpdatesService.fetchUpdates(xAuthToken).getOrElse("Do nothing.")
          // find empty conversations and create an intro task
          matches.filter( m => m.messages.size==0 ).foreach { m =>
            Logger.debug("[tinderbot] Creating new message intro task for match %s" format m._id)
            val task = Props(new MessageIntroTask(xAuthToken, tinderBot, m))
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
