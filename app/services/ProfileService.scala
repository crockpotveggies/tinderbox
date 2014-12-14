package services

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

  /**
   * User cache object.
   */
  private val users: ConcurrentNavigableMap[String, Profile] = MapDB.db.getTreeMap("profiles")

  /**
   * Retrieve a user from cache.
   * @param userId
   * @return
   */
  def fetchProfile(userId: String): Option[Profile] = {
    users.get(userId) match {
      case null =>
        val tinderApi = new TinderApi(Some(userId))
        val result = Await.result(tinderApi.getProfile(userId), 10 seconds)
        result match {
          case Left(error) => None
          case Right(profile) =>
            println(userId)
            storeProfile(profile)
            Some(profile)
        }
      case profile => Some(profile)
    }
  }

  /**
   * Save a user profile.
   * @param profile
   */
  def storeProfile(profile: Profile) { users.putIfAbsent(profile._id, profile) }

  /**
   * Delete a user profile from cache.
   * @param userId
   */
  def deleteProfile(userId: String) { users.remove(userId) }

}
