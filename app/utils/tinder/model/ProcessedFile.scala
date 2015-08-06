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
  width: Int,

  @(JsonProperty@field)("height")
  height: Int,

  @(JsonProperty@field)("url")
  url: String
)
