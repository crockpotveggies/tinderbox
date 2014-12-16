package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits._
import services.{TinderService, ProfileService}
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
    val f = new TinderApi(Some(xAuthToken)).swipePositive(userId)
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
   * Swipes "left" rejecting another user.
   * @param xAuthToken
   * @param userId
   * @return
   */
  def dislike(xAuthToken: String, userId: String) = Action.async { implicit request =>
    val f = new TinderApi(Some(xAuthToken)).swipeNegative(userId)
    f.map { result =>
      result match {
        case Right(r) =>
          Ok(generate(r))
        case Left(e) =>
          InternalServerError
      }
    }
  }

}
