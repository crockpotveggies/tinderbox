package utils.tinder

import java.text.SimpleDateFormat

import play.api.libs.json._
import play.api.libs.ws.tinder._
import scala.concurrent.Future
import java.util.{TimeZone, Date}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.codahale.jerkson

/**
 * Provides easy access to the Tinder API
 */
object TinderApi {

  /**
   * Instantiating this Json object here helps prevent ClassCast errors
   */
  val jsonContext = new jerkson.Json{}

  /**
   * Describes last time an update was retrieved
   */
  var lastActivity = new Date()

  /**
   * Authentication token for requests on user's behalf
   */
  var xAuthToken: Option[String] = None

  /**
   * The current profile's user id
   */
  var userId: Option[String] = None

  /**
   * An auth object explaining defaults about current session
   */
  var currentAuth: Option[model.TinderAuth] = None

  /**
   * Tinder API Host
   */
  val TINDER_HOST = "https://api.gotinder.com"

  /**
   * Headers for a Tinder API Request
   */
  val STANDARD_HEADERS: Seq[(String, String)] = Seq(
    ("User-Agent", "Tinder Android Version 2.2.3"),
    ("os_version","16"),
    ("Content-type", "application/json")
  )
  def tinderHeaders = xAuthToken match {
    case None => STANDARD_HEADERS
    case Some(token) => STANDARD_HEADERS ++ Seq(("X-Auth-Token", token))
  }

  /**
   * Issues a GET request to the tinder API
   * @param path the relative path
   * @param data an object containing extra values
   */
  def tinderGet[T](path: String, data: Option[JsObject]=None)(implicit m: Manifest[T]): Future[Either[model.TinderError,T]]  = {
    val url = TINDER_HOST+"/"+path
    val requestHolder: TinderRequest = TinderWS
      .url(url)
      .withHeaders(tinderHeaders: _*)
    val request = data match {
      case None => requestHolder.get()
      case Some(d) => requestHolder.get(d)
    }
    request
      .map { response =>
        try {
          Right(jsonContext.parse[T](response.body))
        } catch {
          case e: Throwable =>
            println("[Tinder] Parsing failed with: \n"+response.body)
            println(stackTrace(e))
            Left(jsonContext.parse[model.TinderError](response.body))
        }
      }

  }

  /**
   * Issues a POST request to the tinder API
   * @param path the relative path
   * @param data an object containing extra values
   */
  def tinderPost[T](path: String, data: JsObject)(implicit m: Manifest[T]): Future[Either[model.TinderError,T]] = {
    val url = TINDER_HOST+"/"+path
    TinderWS
      .url(url)
      .withHeaders(tinderHeaders: _*)
      .post(data)
      .map { response =>
        try {
          Right(jsonContext.parse[T](response.body.trim))
        } catch {
          case e: com.fasterxml.jackson.databind.JsonMappingException =>
            println("[Tinder] Parsing failed with: \n"+response.body)
            println(stackTrace(e))
            Left(jsonContext.parse[model.TinderError](response.body))
        }
      }
  }

  /**
   * Gets a list of profiles nearby
   * @param limit the maximum number of profiles to fetch
   */
  def getRecommendations(limit: Int) = {
    val url = TINDER_HOST+"/user/recs"
    TinderWS
      .url(url)
      .withHeaders(tinderHeaders: _*)
      .get(Json.obj("limit" -> limit))
      .map { response =>
      try {
        val res = (response.json \ "results")
        val recs = jsonContext.parse[List[model.RecommendedUser]](Json.stringify(res))
        Right(recs)
      } catch {
        case e: Exception =>
          println("[Tinder] Parsing failed with: \n"+response.body)
          println(stackTrace(e))
          Left(jsonContext.parse[model.TinderError](response.body))
      }
    }
  }

  /**
   * Sends a message to a user
   * @param userId the id of the user
   * @param message the message to send
   */
  def sendMessage(userId: String, message: String) = {
    tinderPost[model.MessageOutgoingResult]("user/matches/"+userId, Json.obj("message" -> message))
  }

  /**
   * Swipes left for a user
   * @param userId the id of the user
   */
  def swipeNegative(userId: String) = {
    tinderGet[model.MatchResult]("pass/"+userId)
  }

  /**
   * Swipes right for a user
   * @param userId the id of the user
   */
  def swipePositive(userId: String) = {
    tinderGet[model.MatchResult]("like/"+userId)
  }

  /**
   * Authorize this tinder client
   * @param fbToken the Facebook token. This will be obtained when authenticating the user
   * @param fbId the Facebook user id.
   */
  def authorize(fbToken: String, fbId: String) = {
    val response = tinderPost[model.TinderAuth]("auth", Json.obj("facebook_token" -> fbToken, "facebook_id" -> fbId))
    response.map { r =>
      r match {
        case Left(_) =>
          println("[Tinder] Something has gone wrong while authenticating Tinder.")
        case Right(auth) =>
          xAuthToken = Some(auth.token)
          userId = Some(auth.user._id)
          currentAuth = Some(auth)
      }
    }
    response
  }

  /**
   * Gets a list of new updates. This will be things like new messages, people who liked you, etc.
   */
  def getUpdates = {
    val response = tinderPost[model.Update]("updates", Json.obj("last_activity_date" -> toISO8601(lastActivity)))
    response.map { r => lastActivity = new Date() }
    response
  }

  /**
   * Gets the entire history for the user (all matches, messages, blocks, etc.)
   *
   * NOTE: Old messages seem to not be returned after a certain threshold. Not yet
   * sure what exactly that timeout is. The official client seems to get this update
   * once when the app is installed then cache the results and only rely on the
   * incremental updates
   */
  def getHistory = {
    tinderPost[model.Update]("updates", Json.obj("last_activity_date" -> ""))
  }

  /**
   * Updates the position for this user
   * @param lon the longitude
   * @param lat the latitutde
   */
  def updatePosition(lat: Double, lon: Double) = {
    tinderPost[model.PositionResult]("user/ping", Json.obj("lat" -> lat, "lon" -> lon))
  }

  /**
   * Get user by id
   * @param userId the id of the user
   */
  def getProfile(userId: String) = {
    tinderGet[model.Profile]("user/"+userId)
  }

  /**
   * Utility function for formatting Date into ISO
   * @param time a Date object to be formatted
   */
  def toISO8601(time: Date): String = {
    val x = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    x.setTimeZone(TimeZone.getTimeZone("UTC"))
    x.format(time)
  }

  def stackTrace(e: Throwable): String = {
    val writer = new java.io.StringWriter()
    val printWriter = new java.io.PrintWriter(writer)
    e.printStackTrace(printWriter)
    writer.toString()
  }

}
