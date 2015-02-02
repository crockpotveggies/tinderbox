package services

import akka.actor._
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka, Akka.system
import java.util.NavigableMap
import java.util.concurrent.ConcurrentNavigableMap
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import org.mapdb._
import models.bot._, Throttler._, tasks._

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
class TinderBot(taskWarningThreshold: Int, taskSleepThreshold: Int) extends Actor {

  override def preStart() = {
    Logger.info("[tinderbot] TinderBot has started up")
  }

  def receive = {
    // send commands to the bot
    case BotCommand(command) =>
      Logger.info("[tinderbot] Received command: "+command)
      command match {
        case "idle" => makeIdle
        case "run" => makeRun
        case "sleep" => makeSleep
        case "state" => getState
        case s if s.startsWith("rate=") => setTaskRate(s.split("=")(1).toInt)
      }

    // logic for handling queue state
    case QueueState(queueLength) =>
      queueLength match {
        // no more tasks
        case 0 =>
          if(state.state=="running") {
            TinderService.activeSessions.foreach { xAuthToken =>
              // analyze faces
              botThrottle ! Props(new FacialCheckTask(xAuthToken, self))
              Logger.debug("[tinderbot] created new Facial Check task for token " + xAuthToken)

              // analyze recommendations
              botThrottle ! Props(new RecommendationsTask(xAuthToken, self))
              Logger.debug("[tinderbot] created new Recommendation task for token " + xAuthToken)

              // analyze messages
              botThrottle ! Props(new MessageAnalysisTask(xAuthToken, self))
              Logger.debug("[tinderbot] created new Message Analysis task for token " + xAuthToken)
            }
          }

        // tasks exceed shutdown threshold
        case l if l>taskSleepThreshold =>
          makeIdle
          Logger.warn("[tinderbot] TinderBot is going idle (too many tasks > %s)".format(taskSleepThreshold))

        // tasks exceed warning threshold
        case l if l>taskWarningThreshold =>
          Logger.warn("[tinderbot] TinderBot is under pressure (tasks queue > %s)".format(taskWarningThreshold))

        // everything else
        case l =>
          Logger.info("[tinderbot] Tasks queue size is currently %s".format(l))
      }

    // TinderBot received a task from a sender
    case props: Props =>
      botThrottle ! props

    // someone is sending invalid messages
    case e: Any =>
      Logger.error("[tinderbot] TinderBot received an unknown message")
      Logger.error("[supervisor] Received: \n %s" format e.toString)

  }

  /**
   * Tracks state of the bot for logging and tasks.
   */
  private var state: BotState = new BotState(false, "idle")

  /**
   * Throttler and processor do all of the processing.
   */
  val botThrottle = context.actorOf(Props(new BotThrottle(1 msgsPer (2 seconds), Some(self))), "BotThrottle")
  val botSupervisor = context.actorOf(Props(new BotSupervisor(self)), "BotSupervisor")

  /**
   * Retrieves the current state of the bot.
   * @return bot state
   */
  private def getState: BotState = state

  /**
   * Stops the bot from processing new tasks.
   */
  private def makeIdle {
    botThrottle ! SetTarget(None)
    state = new BotState(false, "idle")
  }

  /**
   * Starts the bot by setting a target.
   */
  private def makeRun {
    botThrottle ! SetTarget(Some(botSupervisor))
    state = new BotState(true, "running")
  }

  /**
   * Puts the bot to sleep for 50 seconds, useful when API limits reached or when we've run
   * out of recommendations.
   */
  private def makeSleep {
    botThrottle ! SetTarget(None)
    state = new BotState(false, "sleeping")
    Thread.sleep(10000)
    makeRun
  }

  /**
   * Starts the bot by setting a target.
   */
  private def setTaskRate(rate: Int) {
    botThrottle ! SetRate(1 msgsPer (rate seconds))
  }

}

object TinderBot {
  /**
   * Active context for TinderBot.
   */
  val context = {
    Thread.currentThread().setContextClassLoader(play.api.Play.classloader)
    Akka.system.actorOf(Props(new TinderBot(taskWarningThreshold = 500, taskSleepThreshold = 1000)), "TinderBot")
  }

  /**
   * Logging queues track past history of bot tasks.
   *
   * NOTE: in this case the key is the user's _id, not the X-Auth-Token.
   */
  private val log: ConcurrentNavigableMap[String, List[BotLog]] = MapDB.db.getTreeMap("bot_log")
  private val log_update_queue: ConcurrentNavigableMap[String, List[BotLog]] = MapDB.db.getTreeMap("bot_log_updates")

  /**
   * Fetch the entire log for a TinderBot user.
   * @param userId
   * @return
   */
  def fetchLog(userId: String): List[BotLog] = {
    log.get(userId) match {
      case null => List()
      case logs => logs
    }
  }

  /**
   * Fetch new log updates (useful for ajax) for a TinderBot user.
   * @param userId
   * @return
   */
  def fetchLogUpdates(userId: String, flush: Boolean=true): List[BotLog] = {
    log_update_queue.get(userId) match {
      case null => List()
      case logs =>
        // once updates are fetched, we optionally flush the queue
        if(flush) log_update_queue.put(userId, List())
        logs
    }
  }

  /**
   * Write a new log entry for TinderBot.
   * @param userId
   * @param entry
   * @return
   */
  def writeLog(userId: String, entry: BotLog): BotLog = {
    val newLog = fetchLog(userId) ++ List(entry)
    log.put(userId, newLog)
    val newUpdates = fetchLogUpdates(userId, false) ++ List(entry)
    log_update_queue.put(userId, newUpdates)
    entry
  }
}
