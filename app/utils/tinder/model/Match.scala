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
  last_activity_date: String
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("",List(new Message()),"")
}
