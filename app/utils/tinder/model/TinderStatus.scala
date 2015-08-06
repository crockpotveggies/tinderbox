package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Represents a status result from the Tinder API.
 * @param status
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class TinderStatus(
  @(JsonProperty@field)("status")
  status: Int
)
