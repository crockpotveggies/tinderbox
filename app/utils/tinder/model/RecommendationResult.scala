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
  val status: Int,

  @(JsonProperty@field)("results")
  val results: List[RecommendedUser]
) {
  /**
   * necessary for object instantiation
   */
  def this() = this(0,List(new RecommendedUser()))
}
