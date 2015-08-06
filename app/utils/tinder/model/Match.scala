package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * A match object in a list of updates.
 * @param _id
 * @param messages
 * @param last_activity_date
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class Match(
  @(JsonProperty@field)("_id")
  _id: String,

  @(JsonProperty@field)("messages")
  messages: List[Message],

  @(JsonProperty@field)("last_activity_date")
  last_activity_date: String,

  @(JsonProperty@field)("participants")
  participants: Option[List[String]],

  @(JsonProperty@field)("closed")
  closed: Option[Boolean],

  @(JsonProperty@field)("dead")
  dead: Option[Boolean],

  @(JsonProperty@field)("message_count")
  message_count: Option[Int],

  @(JsonProperty@field)("common_friend_count")
  common_friend_count: Option[Int],

  @(JsonProperty@field)("common_like_count")
  common_like_count: Option[Int],

  @(JsonProperty@field)("not_following")
  not_following: Option[Map[String,Boolean]],

  @(JsonProperty@field)("profile")
  person: Option[ProfileBrief]

)
