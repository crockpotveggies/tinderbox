package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Represents a profile of an instagram user.
 * @param username
 * @param profile_picture
 * @param photos
 * @param media_count
 * @param last_fetch_time
 * @param completed_initial_fetch
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class InstagramProfile(
  @(JsonProperty@field)("username")
  username: String,

  @(JsonProperty@field)("profile_picture")
  profile_picture: String,

  @(JsonProperty@field)("photos")
  photos: List[InstagramPhoto],

  @(JsonProperty@field)("media_count")
  media_count: Int,

  @(JsonProperty@field)("last_fetch_time")
  last_fetch_time: String,

  @(JsonProperty@field)("completed_initial_fetch")
  completed_initial_fetch: Boolean
)
