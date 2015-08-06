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
case class TinderVersion(
  @(JsonProperty@field)("active_text")
  val active_text: String,

  @(JsonProperty@field)("age_filter")
  val age_filter: String,

  @(JsonProperty@field)("matchmaker")
  val matchmaker: String,

  @(JsonProperty@field)("trending")
  val trending: String,

  @(JsonProperty@field)("trending_active_text")
  val trending_active_text: String
)
