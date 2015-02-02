package controllers

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import models.bot.BotLog
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.libs.iteratee.Enumerator
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits._
import services.{FacialAnalysisService, TinderService}

object FacialAnalysis extends Controller {

  /**
   * Fetches the average face from EigenFaces.
   */
  def fetchAverageFace(xAuthToken: String, dataType: String) = Action.async { implicit request =>
    val f = future { TinderService.fetchSession(xAuthToken) }
    f.map { result =>
      result match {
        case None => BadRequest
        case Some(session) =>
          val file = new java.io.File("data/%s_mean_%s_model.gif" format (session.user._id, dataType))
          val fileContent: Enumerator[Array[Byte]] = Enumerator.fromFile(file)

          SimpleResult(
            header = ResponseHeader(200, Map(CONTENT_LENGTH -> file.length.toString)),
            body = fileContent
          )
      }
    }
  }

  /**
   * Resets facial analysis models.
   */
  def resetEigenModels(xAuthToken: String) = Action.async { implicit request =>
    val f = future { TinderService.fetchSession(xAuthToken) }
    f.map { result =>
      result match {
        case None => BadRequest
        case Some(session) =>
          FacialAnalysisService.resetModels(session.user._id)
          Ok
      }
    }
  }

  /**
   * Returns boolean of whether facial models are in a state to be further examined.
   */
  def checkModelValidity(xAuthToken: String) = Action.async { implicit request =>
    val f = future { TinderService.fetchSession(xAuthToken) }
    f.map { result =>
      result match {
        case None => BadRequest
        case Some(session) =>
          val state = FacialAnalysisService.modelsAreValid(session.user._id)
          Ok(state.toString)
      }
    }
  }

}
