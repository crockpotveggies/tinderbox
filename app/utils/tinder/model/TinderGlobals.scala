package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Global variables for a Tinder app.
 * @param boost_decay
 * @param boost_down
 * @param boot_up
 * @param invite_type
 * @param kontagent
 * @param matchmaker_default_message
 * @param recs_interval
 * @param recs_size
 * @param share_default_text
 * @param sparks
 * @param updates_interval
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class TinderGlobals(
  @(JsonProperty@field)("boost_decay")
  boost_decay: Int,

  @(JsonProperty@field)("boost_down")
  boost_down: Int,

  @(JsonProperty@field)("boost_up")
  boost_up: Int,

  @(JsonProperty@field)("invite_type")
  invite_type: String,

  @(JsonProperty@field)("kontagent")
  kontagent: Boolean,

  @(JsonProperty@field)("matchmaker_default_message")
  matchmaker_default_message: String,

  @(JsonProperty@field)("recs_interval")
  recs_interval: Int,

  @(JsonProperty@field)("recs_size")
  recs_size: Int,

  @(JsonProperty@field)("share_default_text")
  share_default_text: String,

  @(JsonProperty@field)("sparks")
  sparks: Boolean,

  @(JsonProperty@field)("updates_interval")
  updates_interval: Int
) {
  /**
   * necessary for object instantiation
   */
  def this() = this(0,0,0,"",false,"",0,0,"",false,0)
}
