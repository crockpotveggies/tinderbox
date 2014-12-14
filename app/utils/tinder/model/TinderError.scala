package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Represents an error from the Tinder API.
 * @param status
 * @param error
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class TinderError(
  @(JsonProperty@field)("status")
  status: String,

  @(JsonProperty@field)("error")
  error: String
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("","")
}
