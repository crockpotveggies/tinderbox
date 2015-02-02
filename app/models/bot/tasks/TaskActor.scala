package models.bot.tasks

import play.api.Logger
import akka.actor._
import models.bot.BotCommand

/**
 * Template for creating tasks.
 */
trait TaskActor extends Actor {

  /**
   * Tasks require a valid X-Auth-Token for the Tinder API.
   * @return
   */
  def xAuthToken: String

  /**
   * Tasks need a valid reference back to the TinderBot for sending messages.
   * @return
   */
  def tinderBot: ActorRef

  /**
   * Implementing the "run" command ensures that TinderBot delivers the next task.
   */
  override def postStop(): Unit = {
    tinderBot ! BotCommand("run")
  }

}
