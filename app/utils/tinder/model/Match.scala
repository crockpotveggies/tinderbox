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
  val _id: String,

  @(JsonProperty@field)("messages")
  var messages: List[Message],

  @(JsonProperty@field)("last_activity_date")
  val last_activity_date: String,

  @(JsonProperty@field)("participants")
  val participants: Option[List[String]],

  @(JsonProperty@field)("closed")
  val closed: Option[Boolean],

  @(JsonProperty@field)("dead")
  val dead: Option[Boolean],

  @(JsonProperty@field)("message_count")
  var message_count: Option[Int],

  @(JsonProperty@field)("common_friend_count")
  val common_friend_count: Option[Int],

  @(JsonProperty@field)("common_like_count")
  val common_like_count: Option[Int],

  @(JsonProperty@field)("not_following")
  val not_following: Option[Map[String,Boolean]],

  @(JsonProperty@field)("profile")
  var person: Option[ProfileBrief]

) {
  /**
   * necessary for object instantiation
   */
  def this() = this("",List(new Message()),"",None,None,None,None,None,None,None,None)
}
