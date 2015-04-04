package models.bot

import akka.actor._
import play.api.Logger


/**
  * Supervisor that overlooks child actors making requests to
  * external API services
  */
class BotSupervisor(parent: ActorRef) extends Actor {
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1 minute) {
      case e: Exception => {
        // we need to deliver the run command, otherwise it will be permanently paused
        parent ! BotCommand("run")
        Logger.error("[tinderbot] Retrying a troubled supervised task: \n" + e.getMessage)
        Restart
      }
    }

  override def preStart() = {
    Logger.debug("[tinderbot] Bot supervisor has started up")
  }

  def receive = {
    case p: Props =>
      // pause the throttle so we don't have too many concurrent tasks
      parent ! BotCommand("idle")
      // Actor props sent here will be instantiated and supervised
      Logger.debug("[tinderbot] New task received for supervision")
      context.actorOf(p)

    case e: Any =>
      Logger.error("[tinderbot] Supervisor received an unknown message")
      Logger.error("[tinderbot] Received: \n %s" format e.toString)

  }
 }