package services

import akka.actor._
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka, Akka.system
import java.util.NavigableMap
import java.util.concurrent.ConcurrentNavigableMap
import scala.concurrent.duration._
import org.mapdb._
import models.bot._, Throttler._

/**
 * Background service that automates tasks for Tinder.
 *
 * This bot works by instantiating a task throttler which sends commands to an Actor
 * that performs the tasks. The Actor's supervisor reports back to the throttler when it's
 * completed the task and the throttler watches for backpressure. If there is too much
 * backpressure or a Tinder API limitation, the Throttler will set itself to Idle
 * until it can continue work. A supervisor watches the worker for exceptions.
 *
 * NOTE: the bot only starts when the Global object sends a start signal
 */
class TinderBot(taskThreshold: Int) extends Actor {

  override def preStart() = {
    makeRun
    Logger.info("[tinderbot] Bot monitor has started up")
  }

  def receive = {
    case BotCommand(command) =>
      command match {
        case "idle" => makeIdle
        case "run" => makeRun
        case "state" => getState
      }

    case QueueState(queueLength) =>
      queueLength match {
        // no more tasks
        case 0 =>
        // TODO send actors for recommendations
        // tasks are less than 10
        case l if l<10 =>
        // TODO create more actors for recommendations
        // tasks exceed 50
        case l if l>taskThreshold =>
        // TODO set bot state to "pressured"
        // everything else
        case _ =>
        // TODO set bot state to "running"
      }

    case e: Any =>
      Logger.error("[tinderbot] Supervisor received an unknown message")
      Logger.error("[supervisor] Received: \n %s" format e.toString)

  }

  /**
   * Tracks state of the bot for logging and tasks.
   */
  private var state: BotState = new BotState(false, "idle")
  private val log: ConcurrentNavigableMap[String, BotLog] = MapDB.db.getTreeMap("bot_log")
  private val log_update_queue: ConcurrentNavigableMap[String, BotState] = MapDB.db.getTreeMap("bot_log_updates")

  /**
   * Throttler and processor do all of the processing.
   */
  val botThrottle = system.actorOf(Props(new BotThrottle(1 msgsPer (5 seconds), Some(self))), "BotThrottle")
  val botSupervisor = system.actorOf(Props(new BotSupervisor(self)), "BotSupervisor")

  /**
   * Retrieves the current state of the bot.
   * @return bot state
   */
  def getState: BotState = state

  /**
   * Stops the bot from processing new tasks.
   *
   * NOTE: the bot will continue to create new tasks until it has reached a task threshold.
   */
  def makeIdle {
    botThrottle ! SetTarget(None)
    state = new BotState(false, "idle")
  }

  /**
   * Starts the bot by setting a target.
   */
  def makeRun {
    botThrottle ! SetTarget(Some(botSupervisor))
    state = new BotState(true, "running")
  }

}

object TinderBot {
  // instantiate the tinderbot
  val context = Akka.system.actorOf(Props(new TinderBot(taskThreshold = 50)), "TinderBot")
}
