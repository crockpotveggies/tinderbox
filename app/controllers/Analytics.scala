package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import services.AnalyticsService
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits._
import com.codahale.jerkson.Json._


object Analytics extends Controller {

  /**
   * Retrieve message sentiment analytics.
   */
  def messageSentiments(xAuthToken: String) = Action.async { implicit request =>
    val f = future { AnalyticsService.fetchSentiments(xAuthToken) }
    f.map { result =>
      result match {
        case Some(data) =>
          Ok(generate(data)).as("application/json")
        case None =>
          BadRequest
      }
    }
  }

}
