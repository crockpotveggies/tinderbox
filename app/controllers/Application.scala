package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

import utils.SparkMLLibUtility

object Application extends Controller {

  /**
   * index of this presentation
   */
  def index = Action {
    Ok(views.html.index())
  }

  /**
   *
   * @param fileLocation the location of a naive bayes sample training set
   */
  def trainNaiveBayes(fileLocation: String) = Action.async {
    val f = Future {
      SparkMLLibUtility.SparkMLLibExample(fileLocation)
    }

    f.map { result => Ok(Json.toJson(Map("accuracy" -> result))) }
  }

}
