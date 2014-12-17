package models.bot.tasks

import akka.actor._
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits._
import utils.tinder.TinderApi
import utils.tinder.model._

/**
 * Worker task that processes recommendations.
 */
class SwipeTask(val xAuthToken: String, val tinderBot: ActorRef, val rec: RecommendedUser) extends TaskActor {

  override def preStart() = {
    Logger.info("[tinderbot] Starting new swipe task.")
    self ! "tick"
  }

  private val tinderApi = new TinderApi(Some(xAuthToken))

  private def dislikeUser = {
    tinderApi.swipeNegative(rec._id).map { result =>
      result match {
        case Left(e) =>
          Logger.error("[tinderbot] Swipe task had an error on Tinder: "+e.error)

        case Right(r) =>
          Logger.info("[tinderbot] Disliked user "+rec._id)
      }
    }
  }

  private def likeUser = {
    tinderApi.swipeNegative(rec._id).map { result =>
      result match {
        case Left(e) =>
          Logger.error("[tinderbot] Swipe task had an error on Tinder: "+e.error)

        case Right(r) =>
          if(r==null || r==None) Logger.info("[tinderbot] Liked user "+rec._id)
          else Logger.info("[tinderbot] Matched with user "+rec._id)
      }
    }
  }

  def receive = {
    case "tick" =>
      /*
      Here we process a recommended user. The strategy is to eliminate users who
      don't meet certain criteria.
       */
      if(rec.photos.size==2 && rec.bio=="") dislikeUser
      else if (rec.photos.size==1) dislikeUser
      // uncomment the code below if you want to auto-like user
      /*
      else likeUser
       */
      else Logger.info("[tinderbot] Ignored recommended user "+rec._id)


      // make sure we properly shut down this actor
      self ! PoisonPill

    // someone is sending invalid messages
    case e: Any =>
      Logger.error("[tinderbot] Task received an unknown message")
      Logger.error("[tinderbot] Received: \n %s" format e.toString)

  }

}
