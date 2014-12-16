package models

import com.fasterxml.jackson.annotation._
import scala.annotation.meta.field
import utils.tinder.model._


/**
 * A container class for a match and meta-data about updates.
 * @param _id
 * @param `match`
 * @param profile
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class MatchUpdate(
  @(JsonProperty@field)("_id")
  val _id: String,

  @(JsonProperty@field)("messages")
  val `match`: Match,

  @(JsonProperty@field)("profile")
  val profile: Option[Profile]

) {
  /**
   * necessary for object instantiation
   */
  def this() = this("",new Match(),None)
}
