package controllers

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Akka.system
import com.codahale.jerkson.Json._
import utils.tinder.TinderApi
import services.TinderService


object Profile extends Controller {

  implicit val timeout = Timeout(10 seconds)

  /**
   * Render the discovery settings page.
   */
  def discovery(xAuthToken: String) = Action.async { implicit request =>
    val f = future { TinderService.fetchSession(xAuthToken) }
    f.map { result =>
      result match {
        case Some(session) => Ok(views.html.Profile.discovery(session))
        case None => BadRequest
      }
    }
  }

  /**
   * Update the user's current location.
   */
  def getProfile(xAuthToken: String) = Action.async { implicit request =>
    val f = new TinderApi(Some(xAuthToken)).getProfile
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
   * Update the user's current location.
   */
  def updatePosition(xAuthToken: String) = Action.async(parse.json) { implicit request =>
    val lat = (request.body \ "lat").as[Double]
    val lon = (request.body \ "lon").as[Double]

    val f = new TinderApi(Some(xAuthToken)).updatePosition(lat, lon)
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
   * Update the user's discovery preferences.
   */
  def updateDiscovery(xAuthToken: String) = Action.async(parse.json) { implicit request =>
    val ageMin = (request.body \ "age_filter_min").as[Int]
    val ageMax = (request.body \ "age_filter_max").as[Int]
    val distance = (request.body \ "distance_filter").as[Int]
    val gender = (request.body \ "gender").as[Int]
    val interestedIn = (request.body \ "interested_in").as[List[Int]]

    val f = new TinderApi(Some(xAuthToken)).updateDiscoveryPreferences(ageMin, ageMax, distance, gender, interestedIn)
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
