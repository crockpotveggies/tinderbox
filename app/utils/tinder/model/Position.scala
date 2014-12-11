package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * A set of location coordinates.
 * @param lat
 * @param lon
 */
@JsonIgnoreProperties(ignoreUnknown=true)
class Position(
  @(JsonProperty@field)("_id")
  lat: Double,

  @(JsonProperty@field)("_id")
  lon: Double
) {
  /**
   * necessary for object instantiation
   */
  def this() = this(0.0,0.0)
}
