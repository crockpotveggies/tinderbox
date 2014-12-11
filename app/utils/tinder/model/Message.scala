package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Represents a user-to-user message
 * @param _id
 * @param from
 * @param to
 * @param match_id
 * @param sent_date
 * @param message
 * @param created_date
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class Message(
  @(JsonProperty@field)("_id")
  _id: String,

  @(JsonProperty@field)("from")
  from: String,

  @(JsonProperty@field)("to")
  to: String,

  @(JsonProperty@field)("match_id")
  match_id: String,

  @(JsonProperty@field)("sent_date")
  sent_date: String,

  @(JsonProperty@field)("message")
  message: String,

  @(JsonProperty@field)("created_date")
  created_date: String
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("","","","","","","")
}


/**
 * Response from server after sending message.
 * @param from
 * @param to
 * @param message
 * @param created_date
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class MessageOutgoingResult(
  @(JsonProperty@field)("from")
  from: String,

  @(JsonProperty@field)("to")
  to: String,

  @(JsonProperty@field)("message")
  message: String,

  @(JsonProperty@field)("created_date")
  created_date: String
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("","","","")
}
