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
  val _id: String,

  @(JsonProperty@field)("messages")
  val messages: List[Message],

  @(JsonProperty@field)("message_count")
  val message_count: Int,

  @(JsonProperty@field)("common_friend_count")
  val common_friend_count: Int,

  @(JsonProperty@field)("common_like_count")
  val common_like_count: Int,

  @(JsonProperty@field)("last_activity_date")
  val last_activity_date: String,

  @(JsonProperty@field)("created_date")
  val created_date: String,

  @(JsonProperty@field)("participants")
  val participants: List[String],

  @(JsonProperty@field)("closed")
  val closed: Boolean,

  @(JsonProperty@field)("pending")
  val pending: Boolean,

  @(JsonProperty@field)("following")
  val following: Boolean,

  @(JsonProperty@field)("following_moments")
  val following_moments: Boolean,

  @(JsonProperty@field)("dead")
  val dead: Boolean
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("",List(new Message()),0,0,0,"","",List(""),false,false,false,false,false)
}
