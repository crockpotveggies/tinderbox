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
   * A summary of message numbers.
   */
  def messageSummaries(xAuthToken: String) = Action.async { implicit request =>
    val forceHistory = future { UpdatesService.forceHistorySync(xAuthToken) }
    val f = future { UpdatesService.fetchHistory(xAuthToken) }
    f.map { result =>
      result match {
        case Some(history) =>
          val messages = history.map(_.messages).flatten.size
          val matches = history.size
          val extendedConversations = history.map(_.messages).filter { m => m.size > 7 }.size

          Ok(generate(
            Map("total_matches" -> matches, "total_messages" -> messages, "long_talks" -> extendedConversations)
          )).as("application/json")
        case None =>
          BadRequest
      }
    }
  }

  /**
   * Get all message history for a user.
   */
  def messages(xAuthToken: String) = Action.async { implicit request =>
    val f1 = future { UpdatesService.fetchHistory(xAuthToken) }
    val f2 = future { UpdatesService.fetchUnreadCounts(xAuthToken) }

    val result = for {
      history <- f1
      counts <- f2
    } yield (history, counts)

    // check if unread counts are available, and merge
    result.map { r =>
      r match {
        case (Some(h), Some(c)) =>
          val data = h
            .filterNot { m => m.person == None}
            .map{ m => val newM = m.copy(message_count = c.get(m._id)); newM }
          Ok(generate(data)).as("application/json")

        case (Some(h), None) =>
          val data = h.filterNot { m => m.person == None}
          Ok(generate(data)).as("application/json")

        case _ =>
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
        case (Some(n), Some(c)) => Ok(generate(Map("notifications" -> n, "unread_counts" -> c)))
        case (None, None) => Ok(generate(Map("notifications" -> None, "unread_counts" -> None)))
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
