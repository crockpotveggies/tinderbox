package models.bot.tasks

import akka.actor._
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits._
import utils.FacialDetection
import utils.tinder.model._
import utils.tinder.TinderApi
import utils.ImageUtil
import services.FacialAnalysisService

/**
 * This task performs a facial analysis by extracting faces and using k-means to analyze RGB values.
 */
class FacialAnalysisTask(val xAuthToken: String, val tinderBot: ActorRef, val matchUser: String, val swipeType: String) extends TaskActor {

  override def preStart() = {
    Logger.debug("[tinderbot] Starting new facial analysis task for %s." format matchUser)
    self ! "tick"
  }

  def receive = {
    case "tick" =>
      new TinderApi(Some(xAuthToken)).getProfile(matchUser).map { result =>
        result match {
          case Left(error) =>
            Logger.error("[tinderbot] Couldn't retrieve profile for %s for reason %s." format (matchUser, error.toString))

          case Right(profile) =>
            val rgbValues = profile.photos.map { photo =>
              val faces = FacialDetection(photo.url)
              ImageUtil.getNormalizedGrayValues(faces.extractFaces)
            }
            val kMeans = recommendation.FacialAnalysis.kMeans(rgbValues.flatten)

            swipeType match {
              case "yes" =>
                FacialAnalysisService.storeYesVector(matchUser, kMeans)
                Logger.info("[tinderbot] Stored YES vectors for user %s." format matchUser)

              case "no" =>
                FacialAnalysisService.storeNoVector(matchUser, kMeans)
                Logger.info("[tinderbot] Stored NO vectors for user %s." format matchUser)
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
