package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * A processed photo, usually thumbnails of different sizes.
 * @param width
 * @param height
 * @param url
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class ProcessedFile(
  @(JsonProperty@field)("width")
  val width: Int,

  @(JsonProperty@field)("height")
  val height: Int,

  @(JsonProperty@field)("url")
  val url: String
) {
  /**
   * necessary for object instantiation
   */
  def this() = this(0,0,"")
}
