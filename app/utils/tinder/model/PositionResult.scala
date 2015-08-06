package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Result from updating a user position
 * @param status
 * @param error
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class PositionResult(
  @(JsonProperty@field)("status")
  status: Int,

  @(JsonProperty@field)("error")
  error: Option[String]
)
