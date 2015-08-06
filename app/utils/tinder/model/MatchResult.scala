package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Shows whether a like resulted in a match
 * @param match
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class MatchResult(
  @(JsonProperty@field)("match")
  `match`: MatchData
)
