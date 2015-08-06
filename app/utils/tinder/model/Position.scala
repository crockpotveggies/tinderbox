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
case class Position(
  @(JsonProperty@field)("lat")
  lat: Double,

  @(JsonProperty@field)("lon")
  lon: Double
)
