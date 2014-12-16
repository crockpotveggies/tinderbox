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
  val participants: List[String],

  @(JsonProperty@field)("closed")
  val closed: Boolean,

  @(JsonProperty@field)("profile")
  var person: Option[ProfileBrief]

) {
  /**
   * necessary for object instantiation
   */
  def this() = this("",List(new Message()),"",List(""),false,None)
}
