package services

import play.api.Logger
import play.api.Play.current
import scala.collection.mutable.Map
import scala.concurrent._
import scala.concurrent.duration._
import org.mapdb._
import utils.tinder.TinderApi
import utils.tinder.model._
import java.util.concurrent.ConcurrentNavigableMap

/**
 * Intermediary for caching user profiles.
 */
object ProfileService {
  // if we don't set the ClassLoader it will be stuck in SBT
  Thread.currentThread().setContextClassLoader(play.api.Play.classloader)

  /**
   * User cache object.
   */
  private val users: ConcurrentNavigableMap[String, Profile] = MapDB.db.getTreeMap("profile_cache")

  /**
   * Retrieve a user from cache.
   * @param userId
   * @return
   */
  def fetchProfile(xAuthToken: String, userId: String): Option[Profile] = {
    users.get(userId) match {
      case null =>
        val tinderApi = new TinderApi(Some(xAuthToken))
        val result = Await.result(tinderApi.getProfile(userId), 20 seconds)
        result match {
          case Left(error) =>
            Logger.error("Something went wrong when fetching profile for "+userId+": "+error.error)
            None
          case Right(profile) =>
            storeProfile(userId, profile.results)
            Some(profile.results)
        }
      case profile => Some(profile)
    }
  }

  /**
   * Save a user profile.
   * @param profile
   */
  def storeProfile(userId: String ,profile: Profile) { users.put(userId, profile) }

  /**
   * Delete a user profile from cache.
   * @param userId
   */
  def deleteProfile(userId: String) { users.remove(userId) }

}
