package utils.tinder.model

import com.fasterxml.jackson.annotation._
import scala.annotation.meta.field


/**
 * Shows whether a like resulted in a match
 * @param match
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class MatchData(
  @(JsonProperty@field)("_id")
  _id: String,

  @(JsonProperty@field)("messages")
  messages: List[Message],

  @(JsonProperty@field)("message_count")
  message_count: Int,

  @(JsonProperty@field)("common_friend_count")
  common_friend_count: Int,

  @(JsonProperty@field)("common_like_count")
  common_like_count: Int,

  @(JsonProperty@field)("last_activity_date")
  last_activity_date: String,

  @(JsonProperty@field)("created_date")
  created_date: String,

  @(JsonProperty@field)("participants")
  participants: List[String],

  @(JsonProperty@field)("closed")
  closed: Boolean,

  @(JsonProperty@field)("pending")
  pending: Boolean,

  @(JsonProperty@field)("following")
  following: Boolean,

  @(JsonProperty@field)("following_moments")
  following_moments: Boolean,

  @(JsonProperty@field)("dead")
  dead: Boolean
)
