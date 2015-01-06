package models.bot.tasks

import akka.actor._
import play.api.Logger
import play.api.Play.current
import services._


/**
 * Task determines whether facial analysis is needed on a per-user basis.
 */
class FacialCheckTask(val xAuthToken: String, val tinderBot: ActorRef) extends TaskActor {

  override def preStart() = {
    Logger.debug("[tinderbot] Starting new facial check task.")
    self ! "tick"
  }

  private def createFacialAnalysisTask(matchUser: String, swipeType: String) {
    Logger.debug("[tinderbot] Creating facial analysis task for user %s swipe type %s" format (matchUser, swipeType))
    val task = Props(new FacialAnalysisTask(xAuthToken, tinderBot, matchUser, swipeType))
    tinderBot ! task
  }

  def receive = {
    case "tick" =>
      // active session for token
      val session = TinderService.fetchSession(xAuthToken).get

      // grab message history
      UpdatesService.fetchHistory(xAuthToken) match {
        case None =>
          Logger.debug("[tinderbot] Message history was empty.")

        case Some(matches) =>
          // just in case
          Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

          // iterate through each user in conversations if analysis already exists
          matches.filterNot( m => m.person==None ).foreach { m =>
            val matchUser = m.person.get._id
            FacialAnalysisService.fetchYesVector(matchUser) match {
              case Some(o) => // do nothing
              case None =>
                createFacialAnalysisTask(matchUser, "yes")
            }
          }

          // now check the bot logs for users who need analysis
          TinderBot.fetchLog(session.user._id).foreach { log =>
            try {
              log.task match {
                case "undo_swipe_dislike" =>
                  FacialAnalysisService.fetchYesVector(log.associateId.get) match {
                    case Some(o) => // do nothing
                    case None =>
                      createFacialAnalysisTask(log.associateId.get, "yes")
                  }

                case "undo_swipe_like" =>
                  FacialAnalysisService.fetchNoVector(log.associateId.get) match {
                    case Some(o) => // do nothing
                    case None =>
                      createFacialAnalysisTask(log.associateId.get, "no")
                  }

                case "swipe_dislike" =>
                  FacialAnalysisService.fetchNoVector(log.associateId.get) match {
                    case Some(o) => // do nothing
                    case None =>
                      createFacialAnalysisTask(log.associateId.get, "no")
                  }

                case _ =>
                  // do nothing
              }

            } catch {
              case e: Exception =>
                Logger.error("An error occured while analyzing bot logs: "+e.getCause)
            }
          }
      }

      // make sure we properly shut down this actor
      self ! PoisonPill

    // someone is sending invalid messages
    case e: Any =>
      Logger.error("[tinderbot] Task received an unknown message")
      Logger.error("[tinderbot] Received: \n %s" format e.toString)

  }

}
