package models.bot.tasks.recommendation

import play.api.Logger
import models.bot.tasks.recommendation
import services.FacialAnalysisService
import utils.FacialDetection
import utils.tinder.model.Photo
import utils.SparkMLLibUtility.context


/**
 * Helper object for making a "yes" or "no" recommendation for a user's photos.
 *
 * @return an optional boolean representing "yes" or "no"
 */
object FacialRecommendation {
  def makeComparison(userId: String, photos: List[Photo]): Option[Boolean] = {
    val rgbValues = photos.map { photo =>
      val faces = FacialDetection(photo.url)
      recommendation.FacialAnalysis.getRGBValues(faces.extractFaces)
    }
    val kMeansCenters = recommendation.FacialAnalysis.kMeans(rgbValues.flatten)
    val kMeans = context.parallelize(kMeansCenters)
    val yesCost = FacialAnalysisService.yes_kmeans.get.computeCost(kMeans)
    val noCost = FacialAnalysisService.no_kmeans.get.computeCost(kMeans)

    Logger.debug("[tinderbot] Comparison for user %s yielded costs of y=%s n=%s." format (userId, yesCost, noCost))

    // If there's a large variance (i.e.: the image doesn't match either model of yes or no) we simply ignore
    // "true" statement means that a "yes" is recommended
    // Threshold for MSE is set to 6000
    if(yesCost < 6000.00 && noCost < 6000.00) {
      val costDifference = (yesCost-noCost).abs
      if(costDifference > 700.00) {
        val recommendation = yesCost < noCost
        Logger.debug("[tinderbot] Recommendation is %s for user %s." format (recommendation, userId))
        Some(recommendation)
      } else {
        None
      }
    } else {
      Logger.debug("[tinderbot] Recommendation was not significant enough for user %s." format userId)
      None
    }
  }
}