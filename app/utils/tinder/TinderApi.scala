package utils.tinder

import java.text.SimpleDateFormat

import play.api.libs.json._
import play.api.libs.ws.tinder._
import scala.concurrent.Future
import java.util.{TimeZone, Date}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
 * Provides easy access to the Tinder API
 */
class TinderApi(
  var xAuthToken: Option[String] = None
) {

  /**
   * Instantiating this Json object here helps prevent ClassCast errors
   */
  val jsonContext = new ObjectMapper() with ScalaObjectMapper
  jsonContext.registerModule(DefaultScalaModule)

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
    ("os_version", "16"),
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
          Right(jsonContext.readValue[T](response.body))
        } catch {
          case e: Throwable =>
            try {
              // check if this response was a Tinder error
              Left(jsonContext.readValue[model.TinderError](response.body))
            } catch {
              case e: Throwable =>
                // sometimes Tinder wraps these things in a results object
                val res = (response.json \ "results")
                if(res==None || res==null) throw new Exception("Data was not wrapped in a results class. Could not parse.")
                val recs = jsonContext.readValue[T](Json.stringify(res))
                Right(recs)
            }
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
          Right(jsonContext.readValue[T](response.body))
        } catch {
          case e: Throwable =>
            println(e.getCause)
            try {
              // sometimes Tinder wraps these things in a results object
              val res = (response.json \ "results")
              val recs = jsonContext.readValue[T](Json.stringify(res))
              if(recs==null || recs=="null") throw new Exception("Data was not wrapped in a results class. Could not parse.")
              Right(recs)
            } catch {
              case e: Throwable =>
                //println("[Tinder] Parsing failed with: \n" + response.body)
                println(stackTrace(e))
                Left(jsonContext.readValue[model.TinderError](response.body))
            }
        }
      }
  }

  /**
   * Issues a DELETE request to the tinder API
   * @param path the relative path
   */
  def tinderDelete[T](path: String)(implicit m: Manifest[T]): Future[Either[model.TinderError,T]] = {
    val url = TINDER_HOST+"/"+path
    TinderWS
      .url(url)
      .withHeaders(tinderHeaders: _*)
      .delete()
      .map { response =>
      try {
        Right(jsonContext.readValue[T](response.body))
      } catch {
        case e: Throwable =>
          println("[Tinder] Parsing failed with: \n" + response.body)
          println(stackTrace(e))
          Left(jsonContext.readValue[model.TinderError](response.body))
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
        val recs = jsonContext.readValue[List[model.RecommendedUser]](Json.stringify(res))
        Right(recs)
      } catch {
        case e: Exception =>
          println("[Tinder] Parsing failed with: \n"+response.body)
          println(stackTrace(e))
          Left(jsonContext.readValue[model.TinderError](response.body))
      }
    }
  }

  /**
   * Sends a message to a user
   * @param matchId the id of the user
   * @param message the message to send
   */
  def sendMessage(matchId: String, message: String) = {
    tinderPost[model.MessageOutgoingResult]("user/matches/"+matchId, Json.obj("message" -> message))
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
        case Left(e) =>
          println("[Tinder] Something has gone wrong while authenticating Tinder.")
          println("[Tinder] Error was: \n"+e.error)
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
  def getUpdates(lastActivity: Date) = {
    tinderPost[model.Update]("updates", Json.obj("last_activity_date" -> toISO8601(lastActivity)))
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
   * Updates the discovery preferences for this user
   * @param ageMin
   * @param ageMax
   * @param distance
   * @param gender
   * @param interestedIn
   */
  def updateDiscoveryPreferences(ageMin: Int, ageMax: Int, distance: Int, gender: Int, interestedIn: List[Int]) = {
    tinderPost[model.User]("profile",
      Json.obj(
        "age_filter_min" -> ageMin,
        "age_filter_max" -> ageMax,
        "distance_filter" -> distance,
        "gender" -> gender,
        "interested_in" -> interestedIn
      )
    )
  }

  /**
   * Get the current user's profile.
   */
  def getProfile = {
    tinderGet[model.User]("profile")
  }

  /**
   * Get user by id
   * @param userId the id of the user
   */
  def getProfile(userId: String) = {
    tinderGet[model.ProfileResult]("user/"+userId)
  }

  /**
   * Sends a message to a user
   * @param userId the id of the user
   * @param causeId the reason for reporting (1=spam, 2=offensive)
   */
  def reportUser(userId: String, causeId: Int) = {
    tinderPost[model.TinderStatus]("report/"+userId, Json.obj("cause" -> causeId))
  }

  /**
   * Removes a match from user's inbox
   * @param matchId the match ID of user's conversation
   */
  def unmatch(matchId: String) = {
    tinderDelete[model.TinderStatus]("user/matches/"+matchId)
  }

  def ISO8601 = {
    val f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    f.setTimeZone(TimeZone.getTimeZone("UTC"))
    f
  }

  /**
   * Utility function for formatting Date into ISO
   * @param time a Date object to be formatted
   */
  def toISO8601(time: Date): String = {
    ISO8601.format(time)
  }

  def stackTrace(e: Throwable): String = {
    val writer = new java.io.StringWriter()
    val printWriter = new java.io.PrintWriter(writer)
    e.printStackTrace(printWriter)
    writer.toString()
  }

}
