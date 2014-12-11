package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Information about the active user.
 * @param _id
 * @param active_time
 * @param age_filter_max
 * @param api_token
 * @param bio
 * @param birth_date
 * @param create_date
 * @param distance_filter
 * @param distance_filter_min
 * @param facebook_id
 * @param facebook_token
 * @param full_name
 * @param gender
 * @param gender_filter
 * @param interested_in
 * @param ip_address
 * @param last_fb_sync_date
 * @param location
 * @param age_filter_min
 * @param name
 * @param pending
 * @param photos
 * @param ping_time
 * @param pos
 * @param user_number
 */
@JsonIgnoreProperties(ignoreUnknown=true)
class User(
  @(JsonProperty@field)("_id")
  val _id: String,

  @(JsonProperty@field)("active_time")
  val active_time: String,

  @(JsonProperty@field)("age_filter_max")
  val age_filter_max: Int,

  @(JsonProperty@field)("api_token")
  val api_token: String,

  @(JsonProperty@field)("bio")
  val bio: String,

  @(JsonProperty@field)("birth_date")
  val birth_date: String,

  @(JsonProperty@field)("create_date")
  val create_date: String,

  @(JsonProperty@field)("distance_filter")
  val distance_filter: Int,

  @(JsonProperty@field)("distance_filter_min")
  val distance_filter_min: Int,

  @(JsonProperty@field)("facebook_id")
  val facebook_id: String,

  @(JsonProperty@field)("facebook_token")
  val facebook_token: String,

  @(JsonProperty@field)("full_name")
  val full_name: String,

  @(JsonProperty@field)("gender")
  val gender: Int,

  @(JsonProperty@field)("gender_filter")
  val gender_filter: Int,

  @(JsonProperty@field)("interested_in")
  val interested_in: List[Int],

  @(JsonProperty@field)("ip_address")
  val ip_address: String,

  @(JsonProperty@field)("last_fb_sync_date")
  val last_fb_sync_date: String,

  @(JsonProperty@field)("location")
  val location: Location,

  @(JsonProperty@field)("age_filter_min")
  val age_filter_min: Int,

  @(JsonProperty@field)("name")
  val name: String,

  @(JsonProperty@field)("pending")
  val pending: Boolean,

  @(JsonProperty@field)("photos")
  val photos: List[Photo],

  @(JsonProperty@field)("ping_time")
  val ping_time: String,

  @(JsonProperty@field)("pos")
  val pos: Position,

  @(JsonProperty@field)("user_number")
  val user_number: Int
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("","",0,"","","","",0,0,"","","",0,0,List(0),"","",new Location(),0,"",false,List(new Photo()),"",new Position(),0)
}
