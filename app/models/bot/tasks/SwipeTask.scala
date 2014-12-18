package models.bot.tasks

import akka.actor._
import play.api.Logger
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits._
import java.util.Date
import utils.tinder.TinderApi
import utils.tinder.model._
import utils.FacialDetection

/**
 * Worker task that processes recommendations.
 */
class SwipeTask(val xAuthToken: String, val tinderBot: ActorRef, val rec: RecommendedUser, val autoLike: Boolean=false) extends TaskActor {

  override def preStart() = {
    Logger.debug("[tinderbot] Starting new swipe task.")
    self ! "tick"
  }

  private val tinderApi = new TinderApi(Some(xAuthToken))

  private def dislikeUser(reason: String) = {
    tinderApi.swipeNegative(rec._id).map { result =>
      result match {
        case Left(e) =>
          Logger.error("[tinderbot] Swipe task had an error on Tinder: "+e.error)

        case Right(r) =>
          Logger.info("[tinderbot] Disliked user "+rec._id)
          Logger.debug("[tinderbot] User was disliked because: "+reason)
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

  private def photoCriteria(photos: List[Photo]): Boolean = {
    val facesPerPhoto: List[Int] = photos.map { photo => FacialDetection(photo.url).countFaces }
    Logger.debug("[tinderbot] Number of faces for user %s: %s".format(rec._id, facesPerPhoto.toString))
    if(facesPerPhoto.find(p => p==1) == None) false // no singular photo of themselves
    else if(facesPerPhoto.sum==0) false // can't find a face in any photo
    else true
  }

  def receive = {
    case "tick" =>
      // some initial criteria
      val day = 86400000L
      val lastSeenAgo = {
        val now = System.currentTimeMillis
        val lastSeen = tinderApi.ISO8601.parse(rec.ping_time).getTime
        now - lastSeen
      }

      /*
      Here we assess a recommended user. The strategy is to eliminate users who
      don't meet certain criteria.
       */
      if(rec.photos.size==2 && rec.bio=="") dislikeUser("sparse photos, no bio")
      else if (rec.photos.size==1) dislikeUser("sparse photos")
      else if (lastSeenAgo > (day*3)) dislikeUser("hasn't been active for %s days".format((lastSeenAgo/day)))
      else if (!photoCriteria(rec.photos)) dislikeUser("failed photo criteria")
      else if (rec.bio.matches("no.{0,15}hookups")) likeUser
      else if (autoLike) likeUser
      else Logger.info("[tinderbot] Ignored recommended user "+rec._id)


      // make sure we properly shut down this actor
      self ! PoisonPill

    // someone is sending invalid messages
    case e: Any =>
      Logger.error("[tinderbot] Task received an unknown message")
      Logger.error("[tinderbot] Received: \n %s" format e.toString)

  }

}
