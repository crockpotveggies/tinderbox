package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Information about the active user.
 * @param _id
 * @param age_filter_max
 * @param api_token
 * @param bio
 * @param birth_date
 * @param create_date
 * @param distance_filter
 * @param distance_filter_min
 * @param facebook_id
 * @param full_name
 * @param gender
 * @param gender_filter
 * @param interested_in
 * @param ip_address
 * @param location
 * @param age_filter_min
 * @param name
 * @param photos
 * @param ping_time
 * @param pos
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class User(
  @(JsonProperty@field)("_id")
  _id: String,

  @(JsonProperty@field)("age_filter_min")
  age_filter_min: Int,

  @(JsonProperty@field)("age_filter_max")
  age_filter_max: Int,

  @(JsonProperty@field)("api_token")
  api_token: String,

  @(JsonProperty@field)("bio")
  bio: String,

  @(JsonProperty@field)("birth_date")
  birth_date: String,

  @(JsonProperty@field)("create_date")
  create_date: String,

  @(JsonProperty@field)("distance_filter")
  distance_filter: Int,

  @(JsonProperty@field)("distance_filter_min")
  distance_filter_min: Option[Int],

  @(JsonProperty@field)("facebook_id")
  facebook_id: String,

  @(JsonProperty@field)("full_name")
  full_name: Option[String],

  @(JsonProperty@field)("gender")
  gender: Int,

  @(JsonProperty@field)("gender_filter")
  gender_filter: Int,

  @(JsonProperty@field)("interested_in")
  interested_in: List[Int],

  @(JsonProperty@field)("ip_address")
  ip_address: Option[String],

  @(JsonProperty@field)("location")
  location: Option[Location],

  @(JsonProperty@field)("name")
  name: String,

  @(JsonProperty@field)("photos")
  photos: List[Photo],

  @(JsonProperty@field)("ping_time")
  ping_time: String,

  @(JsonProperty@field)("pos")
  pos: Position,

  @(JsonProperty@field)("discoverable")
  discoverable: Option[Boolean]
)
