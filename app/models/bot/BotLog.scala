package models.bot

import com.fasterxml.jackson.annotation._
import scala.annotation.meta.field
import utils.tinder.model._


/**
 * Represents user-controlled state of the bot.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class BotLog(
  @(JsonProperty@field)("created")
  val created: Long,

  @(JsonProperty@field)("task")
  val task: String,

  @(JsonProperty@field)("log")
  val log: String,

  @(JsonProperty@field)("associateId")
  val associateId: Option[String],

  @(JsonProperty@field)("associateImg")
  val associateImg: Option[String]

) {
  /**
   * necessary for object instantiation
   */
  def this() = this(0L,"","",None,None)
}
