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


object People extends Controller {

  /**
   * Pulls up an active Tinder session in a dashboard.
   */
  def profile(xAuthToken: String, userId: String) = Action.async { implicit request =>
    val f = future { ProfileService.fetchProfile(userId) }
    f.map { result =>
      result match {
        case Some(o) => Ok(generate(o)).as("application/json")
        case None => BadRequest
      }
    }
  }

  /**
   * Report a user's profile if it is spamming or offensive.
   * @param xAuthToken
   * @param userId
   * @param causeId
   * @return
   */
  def report(xAuthToken: String, userId: String, causeId: Int) = Action.async { implicit request =>
    val f = new TinderApi(Some(xAuthToken)).reportUser(userId, causeId)
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
   * Removes a match from your inbox.
   * @param xAuthToken
   * @param matchId
   * @return
   */
  def unmatch(xAuthToken: String, matchId: String) = Action.async { implicit request =>
    val f = new TinderApi(Some(xAuthToken)).unmatch(matchId)
    f.map { result =>
      result match {
        case Right(result) =>
          Ok(generate(result)).as("application/json")
        case Left(e) =>
          BadRequest
      }
    }
  }

}
