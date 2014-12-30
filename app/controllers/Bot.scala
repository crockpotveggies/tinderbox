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
import models.bot.{BotLog, BotState, BotCommand}
import services.{TinderBot, TinderService}


object Bot extends Controller {

  implicit val timeout = Timeout(10 seconds)

  /**
   * Pulls up an active Tinder session in a dashboard.
   */
  def bot(xAuthToken: String) = Action.async { implicit request =>
    val f = future { TinderService.fetchSession(xAuthToken) }
    f.map { result =>
      result match {
        case Some(session) => Ok(views.html.Bot.bot(session))
        case None => BadRequest
      }
    }
  }

  /**
   * Get the current state of the Bot.
   */
  def state = Action.async { implicit request =>
    val f = TinderBot.context ? BotCommand("state")
    f.map { result => Ok(generate(result)).as("application/json") }
  }

  /**
   * Get the current state of the Bot.
   */
  def userLog(userId: String) = Action.async { implicit request =>
    val f = future { TinderBot.fetchLog(userId) }
    f.map { result => Ok(generate(result)).as("application/json") }
  }

  /**
   * Get the current state of the Bot.
   */
  def userLogUpdates(userId: String) = Action.async { implicit request =>
    val f = future { TinderBot.fetchLogUpdates(userId) }
    f.map { result => Ok(generate(result)).as("application/json") }
  }

  /**
   * Manually create a bot log. Useful for reversing bot actions.
   *
   * @note You will need to send a raw json string in the BotLog class format.
   */
  def createLog(userId: String) = Action.async(parse.text) { implicit request =>
    val log = com.codahale.jerkson.Json.parse[BotLog](request.body)
    val f = future { TinderBot.writeLog(userId, log) }
    f.map { result => Ok(generate(result)).as("application/json") }
  }

}
