package controllers

import models.bot.BotLog
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits._
import services.{FacialAnalysisService, TinderService, ProfileService, TinderBot}
import utils.tinder.TinderApi
import utils.tinder.model._
import com.codahale.jerkson.Json._


object Recommendations extends Controller {

  /**
   * The recommendations page.
   */
  def matchmaker(xAuthToken: String) = Action.async { implicit request =>
    val f = future { TinderService.fetchSession(xAuthToken) }
    f.map { result =>
      result match {
        case Some(session) => Ok(views.html.Recommendations.matchmaker(session))
        case None => BadRequest
      }
    }
  }

  /**
   * Retrieve a new list of recommendations.
   */
  def recommendations(xAuthToken: String) = Action.async { implicit request =>
    val f = new TinderApi(Some(xAuthToken)).getRecommendations(40)
    f.map { result =>
      result match {
        case Right(result) =>
          Ok(generate(result)).as("application/json")
        case Left(e) =>
          BadRequest
      }
    }
  }

  /**
   * Swipes "right" in favor of another user.
   * @param xAuthToken
   * @param userId
   * @return
   */
  def like(xAuthToken: String, userId: String) = Action.async { implicit request =>
    val tinderApi = new TinderApi(Some(xAuthToken))

    // save yesno data
    val session = TinderService.fetchSession(xAuthToken).get
    FacialAnalysisService.storeYesNoData(session.user._id, userId, true)

    tinderApi.getProfile(userId).map { r =>
      r match {
        case Right(rec) =>
          val log = BotLog(
            System.currentTimeMillis(),
            "swipe_like",
            "Liked %s because: %s.".format(rec.results.name, "user manually selected"),
            Some(rec.results._id),
            Some(rec.results.photos.head.url)
          )
          TinderBot.writeLog(session.user._id, log)

        case _ =>
          Logger.warn("[tinderbot] Failed to write swipe log for user %s." format userId)
      }
    }

    tinderApi.swipePositive(userId).map { result =>
      result match {
        case Right(r) =>
          Ok(generate(r))
        case Left(e) =>
          Ok("null")
      }
    }
  }

  /**
   * Swipes "left" rejecting another user.
   * @param xAuthToken
   * @param userId
   * @return
   */
  def dislike(xAuthToken: String, userId: String) = Action.async { implicit request =>
    val tinderApi = new TinderApi(Some(xAuthToken))

    // save yesno data
    val session = TinderService.fetchSession(xAuthToken).get
    FacialAnalysisService.storeYesNoData(session.user._id, userId, false)

    tinderApi.getProfile(userId).map { r =>
      r match {
        case Right(rec) =>
          val log = BotLog(
            System.currentTimeMillis(),
            "swipe_dislike",
            "Disliked %s because: %s.".format(rec.results.name, "user manually selected"),
            Some(rec.results._id),
            Some(rec.results.photos.head.url)
          )
          TinderBot.writeLog(session.user._id, log)

        case _ =>
          Logger.warn("[tinderbot] Failed to write swipe log for user %s." format userId)
      }
    }

    val f = tinderApi.swipeNegative(userId)
    f.map { result =>
      result match {
        case Right(r) =>
          Ok(generate(r))
        case Left(e) =>
          InternalServerError
      }
    }
  }

  /**
   * Manual input for adding a profile to yes/no models.
   * @param xAuthToken
   * @param userId
   * @param isLike
   */
  def storeYesNoData(xAuthToken: String, userId: String, isLike: Boolean) = Action.async { implicit request =>
    val f = future {
      val session = TinderService.fetchSession(xAuthToken).get
      FacialAnalysisService.storeYesNoData(session.user._id, userId, isLike)
    }
    f.map { result => Ok }
  }

}
