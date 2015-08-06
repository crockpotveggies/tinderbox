package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Represents a single user in a recommendation queue.
 * @param _id
 * @param distance_mi
 * @param common_like_count
 * @param common_friend_count
 * @param bio
 * @param birth_date
 * @param gender
 * @param name
 * @param ping_time
 * @param photos
 * @param birth_date_info
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class RecommendedUser(
  @(JsonProperty@field)("_id")
  val _id: String,

  @(JsonProperty@field)("bio")
  val bio: String,

  @(JsonProperty@field)("birth_date")
  val birth_date: String,

  @(JsonProperty@field)("birth_date_info")
  val birth_date_info: String,

  @(JsonProperty@field)("common_friend_count")
  val common_friend_count: Int,

  @(JsonProperty@field)("common_friends")
  val common_friends: List[Option[String]],

  @(JsonProperty@field)("common_like_count")
  val common_like_count: Int,

  @(JsonProperty@field)("common_likes")
  val common_likes: List[String],

  @(JsonProperty@field)("distance_mi")
  val distance_mi: Int,

  @(JsonProperty@field)("gender")
  val gender: Int,

  @(JsonProperty@field)("instagram")
  val instagram: Option[InstagramProfile],

  @(JsonProperty@field)("name")
  val name: String,

  @(JsonProperty@field)("photos")
  val photos: List[Photo],

  @(JsonProperty@field)("ping_time")
  val ping_time: String
)
