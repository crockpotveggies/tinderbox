package models.bot.tasks

import akka.actor._
import scala.concurrent._
import scala.concurrent.duration._
import play.api.Logger
import utils.tinder.TinderApi
import utils.tinder.model._

/**
 * Worker task that processes recommendations.
 */
class RecommendationsTask(val xAuthToken: String, val tinderBot: ActorRef) extends TaskActor {

  override def preStart() = {
    Logger.info("[tinderbot] Starting new recommendations task.")
    self ! "tick"
  }

  def receive = {
    case "tick" =>
      // fetch a list of recommendations
      val tinderApi = new TinderApi(Some(xAuthToken))
      val recs = Await.result(tinderApi.getRecommendations(40), 20 seconds)
      recs match {
        case Left(e) =>
          Logger.error("[tinderbot] Recommendation task had an error on Tinder: "+e.error)

        case Right(r) =>
          // create a new worker task for each recommendation
          r.foreach { rec =>
            Logger.info("[tinderbot] Creating new swipe task for user %s" format rec._id)
            val task = Props(new SwipeTask(xAuthToken, tinderBot, rec))
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
