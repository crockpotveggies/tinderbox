package services

import java.util.concurrent.ConcurrentNavigableMap
import play.api.Logger
import play.api.Play.current
import scala.collection.mutable.Map
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._
import org.mapdb._
import utils.tinder.TinderApi
import utils.tinder.model._

/**
 * Manages state in-memory of sessions for Tinder.
 */
object TinderService {
  // if we don't set the ClassLoader it will be stuck in SBT
  Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

  /**
   * Current active tokens.
   */
  private val sessions: ConcurrentNavigableMap[String, TinderAuth] = MapDB.db.getTreeMap("sessions")

  /**
   * Retrieve an active session.
   * @param xAuthToken
   * @return
   */
  def fetchSession(xAuthToken: String): Option[TinderAuth] = {
    sessions.get(xAuthToken) match {
      case null =>
        Logger.info("Creating new session for xAuthToken %s".format(xAuthToken))
        val tinderApi = new TinderApi(Some(xAuthToken))
        val result = Await.result(tinderApi.getProfile, 10 seconds)
        result match {
          case Left(error) => None
          case Right(profile) =>
            // create a placeholder auth object
            val tinderAuth = new TinderAuth(xAuthToken,new TinderGlobals,profile,new TinderVersion)
            // save it
            val f = future { storeSession(tinderAuth) }
            Some(tinderAuth)
        }
      case session => Some(session)
    }
  }

  /**
   * Save an active session asynchronously.
   * @param tinderAuth
   */
  def storeSession(tinderAuth: TinderAuth) { sessions.put(tinderAuth.token, tinderAuth) }

  /**
   * End an active session asynchronously.
   * @param xAuthToken
   */
  def deleteSession(xAuthToken: String) { sessions.remove(xAuthToken) }

  /**
   * Retrieve all active tokens
   */
  def activeSessions = {
    sessions.keySet()
  }

}
