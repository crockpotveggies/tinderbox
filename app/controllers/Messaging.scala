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


object Messaging extends Controller {

  /**
   * Pulls up an active Tinder session in a dashboard.
   */
  def inbox(xAuthToken: String) = Action.async { implicit request =>
    val f = future { TinderService.fetchSession(xAuthToken) }
    f.map { result =>
      result match {
        case Some(session) => Ok(views.html.Messaging.inbox(session))
        case None => BadRequest
      }
    }
  }

  /**
   * Pulls up an active Tinder session in a dashboard.
   */
  def messages(xAuthToken: String) = Action.async { implicit request =>
    val f = new TinderApi(Some(xAuthToken)).getHistory
    f.map { result =>
      result match {
        case Right(history) =>
          Ok(generate(history.matches)).as("application/json")
        case Left(e) =>
          BadRequest
      }
    }
  }

  /**
   * Home page let's you select active Tinder sessions or create one.
   */
  val messageForm = Form(
    single(
      "message" -> text(minLength = 1)
    )
  )
  def sendMessage(xAuthToken: String, userId: String) = Action.async { implicit request =>
    messageForm.bindFromRequest.fold(
      formWithErrors => {
        future { BadRequest(generate(Map("error" -> "Your message had errors."))).as("application/json") }
      },
      message => {
        new TinderApi(Some(xAuthToken)).sendMessage(userId, message).map { result =>
          result match {
            case Left(error) =>
              BadRequest(generate(Map("error" -> "Could not send message to Tinder."))).as("application/json")

            case Right(message) =>
              Ok(generate(message)).as("application/json")
          }
        }
      }
    )
  }

}
