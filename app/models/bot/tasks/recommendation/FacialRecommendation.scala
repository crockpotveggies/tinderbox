package models.bot.tasks.recommendation

import play.api.Logger
import models.bot.tasks.recommendation
import services.{SparkService, FacialAnalysisService}
import services.FacialAnalysisService.DEFAULT_FACE_SIZE
import utils.ImageUtil
import utils.face.{EigenFaces, FacialDetection}
import utils.tinder.model.Photo
import SparkService.context


/**
 * Helper object for making a "yes" or "no" recommendation for a user's photos.
 *
 * @return an optional boolean representing "yes" or "no"
 */
object FacialRecommendation {

  /**
   * Makes a comparison by examining similarity using EigenFaces distance to yes/no models.
   * @param userId
   * @param matchUser
   * @param photos
   */
  def makeComparison(userId: String, matchUser: String, photos: List[Photo]): Option[Boolean] = {
    val facePixels = photos.map { photo =>
      val faces = FacialDetection(photo.url)
      if(faces.countFaces==1) { faces.extractFaces.map( face => ImageUtil.getNormalizedImagePixels(face, DEFAULT_FACE_SIZE, DEFAULT_FACE_SIZE)) }
      else List()
    }.flatten

    var yesDistances: Double = 0.0
    var noDistances: Double = 0.0

    try {
      facePixels.map { pixels =>
        val yesModels = FacialAnalysisService.yes_models.get(userId)
        val noModels = FacialAnalysisService.no_models.get(userId)
        val yesDistance = EigenFaces.computeDistance(yesModels._1, yesModels._2, pixels)
        val noDistance = EigenFaces.computeDistance(noModels._1, noModels._2, pixels)
        yesDistances += yesDistance
        noDistances += noDistance
      }

      // calculate the average distance
      yesDistances = yesDistances / facePixels.size
      noDistances = noDistances / facePixels.size

      Logger.debug("[tinderbot] Comparison for user %s yielded distances of y=%s n=%s." format(userId, yesDistances, noDistances))

      // a good threshold for distance is 70.0 since anything higher may be a false positive
      if(yesDistances < 70.0 || noDistances < 70.0) {
        Some(yesDistances < noDistances)

      } else {
        None
      }

    } catch {
      case e: Throwable =>
        Logger.debug("[tinderbot] Recommendation was not significant enough for user %s." format userId)
        None
    }
  }

}