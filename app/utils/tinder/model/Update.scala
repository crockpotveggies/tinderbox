package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Represents history in user timeline.
 * @param matches
 * @param blocks
 * @param deleted_lists
 * @param last_activity_date
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class Update(
  @(JsonProperty@field)("matches")
  matches: List[Match],

  @(JsonProperty@field)("blocks")
  blocks: List[String],

  @(JsonProperty@field)("deleted_lists")
  deleted_lists: List[Map[String, String]],

  @(JsonProperty@field)("last_activity_date")
  last_activity_date: String,

  @(JsonProperty@field)("lists")
  lists: Option[List[Map[String, String]]]
) {
  /**
   * necessary for object instantiation
   */
  def this() = this(List(new Match()),List(""),List(Map(""->"")),"",None)
}
