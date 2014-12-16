package models

import com.fasterxml.jackson.annotation._

import scala.annotation.meta.field


/**
 * A simple notification about updates.
 * @param notificationType
 * @param content
 * @param size
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class Notification(
  @(JsonProperty@field)("notificationType")
  val notificationType: String,

  @(JsonProperty@field)("associateId")
  val associateId: String,

  @(JsonProperty@field)("actorName")
  val actorName: String,

  @(JsonProperty@field)("content")
  var content: String,

  @(JsonProperty@field)("size")
  var size: Int

) {
  /**
   * necessary for object instantiation
   */
  def this() = this("","","","",0)
}
