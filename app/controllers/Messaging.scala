package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits._
import services.{TinderService, UpdatesService}
import utils.tinder.TinderApi
import utils.tinder.model._
import com.codahale.jerkson.Json._
import models._


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
   * Get all message history for a user.
   */
  def messages(xAuthToken: String) = Action.async { implicit request =>
    val f = future { UpdatesService.fetchHistory(xAuthToken) }
    f.map { result =>
      result match {
        case Some(history) =>
          Ok(generate(history)).as("application/json")
        case None =>
          BadRequest
      }
    }
  }

  /**
   * Get updates for a user (supplements a full history).
   * @param xAuthToken
   * @return
   */
  def updates(xAuthToken: String) = Action.async { implicit request =>
    val f = future { UpdatesService.fetchUpdates(xAuthToken) }
    f.map { result =>
      result match {
        case Some((u, n, c)) => Ok(generate(Map("messages" -> u, "notifications" -> n, "unread_counts" -> c)))
        case _ => NotModified
      }
    }
  }

  /**
   * Get notifications for a user.
   * @param xAuthToken
   * @return
   */
  def getNotifications(xAuthToken: String) = Action.async { implicit request =>
    val f = future { UpdatesService.fetchNotifications(xAuthToken) }
    f.map { result =>
      result match {
        case Some(n) => Ok(generate(n))
        case _ => NotModified
      }
    }
  }

  /**
   * Clears all notifications for a user.
   * @param xAuthToken
   * @return
   */
  def clearNotifications(xAuthToken: String) = Action.async { implicit request =>
    val f = future { UpdatesService.clearNotifications(xAuthToken) }
    f.map { result => Ok }
  }

  /**
   * Get unread counts for messages.
   * @param xAuthToken
   * @return
   */
  def getUnreadCounts(xAuthToken: String) = Action.async { implicit request =>
    val f = future { UpdatesService.fetchUnreadCounts(xAuthToken) }
    f.map { result =>
      result match {
        case Some(c) => Ok(generate(c.values.toList))
        case _ => NotModified
      }
    }
  }

  /**
   * Clears the unread counts for a specific message.
   * @param xAuthToken
   * @param matchId
   * @return
   */
  def clearUnreadCount(xAuthToken: String, matchId: String) = Action.async { implicit request =>
    val f = future { UpdatesService.clearUnreadCount(xAuthToken, matchId) }
    f.map { result => Ok }
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
