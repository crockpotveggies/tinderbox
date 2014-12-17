package models.bot.tasks

import akka.actor._

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

}
