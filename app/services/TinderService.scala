package services

import scala.collection.mutable.Map
import utils.tinder.model._

/**
 * Manages state in-memory of sessions for Tinder.
 */
object TinderService {

  /**
   * Current active tokens.
   */
  private val sessions: Map[String, TinderAuth] = Map()

  /**
   * Retrieve an active session.
   * @param xAuthToken
   * @return
   */
  def fetchSession(xAuthToken: String): Option[TinderAuth] = sessions.get(xAuthToken)

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

}
