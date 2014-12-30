package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import services.AnalyticsService
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits._
import com.codahale.jerkson.Json._
import utils.DateToChart


object Analytics extends Controller {

  /**
   * Format a list of ISO date strings into buckets.
   */
  def isoBuckets(bucketType: String, timezoneOffset: Int) = Action.async(parse.json){ implicit request =>
    val f = future {
      var dates = (request.body \ "dates").as[List[String]]
      bucketType match {
        case "daily" =>
          Ok(generate(DateToChart.isoDailyBuckets(dates, timezoneOffset)))
        case "hour" =>
          Ok(generate(DateToChart.isoHourBuckets(dates, timezoneOffset)))
        case _ =>
          BadRequest("Invalid bucket type specified.")
      }

    }
    f.map { result => result }
  }

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
