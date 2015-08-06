package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Represents a list of recommendations.
 * @param status
 * @param results
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class RecommendationResult(
  @(JsonProperty@field)("status")
  status: Int,

  @(JsonProperty@field)("results")
  results: List[RecommendedUser]
)
