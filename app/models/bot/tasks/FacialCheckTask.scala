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

  val session = TinderService.fetchSession(xAuthToken).get

  private def createFacialAnalysisTask(matchUser: String, swipeType: String) {
    Logger.debug("[tinderbot] Creating facial analysis task for user %s swipe type %s" format (matchUser, swipeType))
    val task = Props(new FacialAnalysisTask(xAuthToken, tinderBot, session.user._id, matchUser, swipeType))
    tinderBot ! task
  }

  def receive = {
    case "tick" =>
      // grab message history and analyze each match as a "yes"
      /*UpdatesService.fetchHistory(xAuthToken) match {
        case None =>
          Logger.debug("[tinderbot] Message history was empty.")

        case Some(matches) =>
          // just in case
          Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

          // iterate through each user in conversations if analysis already exists
          matches.filterNot( m => m.person==None ).foreach { m =>
            val matchUser = m.person.get._id
            FacialAnalysisService.fetchYesPixels(session.user._id, matchUser) match {
              case Some(o) => // do nothing
              case None =>
                createFacialAnalysisTask(matchUser, "yes")
            }
          }
      }*/

      // check the manual selection data from the user to see if any need processing
      FacialAnalysisService.fetchYesNoData(session.user._id) match {
        case None =>
          Logger.warn("[tinderbot] No manual yes/no data available, recommendations may be inaccurate.")

        case Some(data) =>
          data.foreach { case (matchUser, isLike) =>
            try {
              isLike match {
                case true =>
                  FacialAnalysisService.fetchYesPixels(session.user._id, matchUser) match {
                    case Some(o) => // do nothing
                    case None => createFacialAnalysisTask(matchUser, "yes")
                  }

                case false =>
                  FacialAnalysisService.fetchNoPixels(session.user._id, matchUser) match {
                    case Some(o) => // do nothing
                    case None => createFacialAnalysisTask(matchUser, "no")
                  }
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
