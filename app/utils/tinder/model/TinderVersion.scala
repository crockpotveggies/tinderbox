package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Tinder version variables.
 * @param active_text
 * @param age_filter
 * @param matchmaker
 * @param trending
 * @param trending_active_text
 */
@JsonIgnoreProperties(ignoreUnknown=true)
class TinderVersion(
  @(JsonProperty@field)("active_text")
  active_text: String,

  @(JsonProperty@field)("age_filter")
  age_filter: String,

  @(JsonProperty@field)("matchmaker")
  matchmaker: String,

  @(JsonProperty@field)("trending")
  trending: String,

  @(JsonProperty@field)("trending_active_text")
  trending_active_text: String
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("","","","","")
}
