package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * A location object, usually for cities.
 * @param id
 * @param name
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class Location(
  @(JsonProperty@field)("id")
  id: String,

  @(JsonProperty@field)("name")
  name: String
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("","")
}
