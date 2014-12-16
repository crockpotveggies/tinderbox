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
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 hour) {
      case _                                    => Restart
    }

  override def preStart() = {
    Logger.info("[tinderbot] Bot supervisor has started up")
  }

  def receive = {
    case p: Props =>
      // Actor props sent here will be instantiated and supervised
      context.actorOf(p)

    case e: Any =>
      Logger.error("[tinderbot] Supervisor received an unknown message")
      Logger.error("[tinderbot] Received: \n %s" format e.toString)

  }
 }