package models.bot

import com.fasterxml.jackson.annotation._
import scala.annotation.meta.field
import utils.tinder.model._


/**
 * Represents user-controlled state of the bot.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class BotState(
  @(JsonProperty@field)("keepActive")
  val keepActive: Boolean,

  @(JsonProperty@field)("state")
  val state: String

) {
  /**
   * necessary for object instantiation
   */
  def this() = this(false,"")
}
