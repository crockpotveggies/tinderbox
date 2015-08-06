package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * A photo in a user's profile.
 * @param id
 * @param main
 * @param crop
 * @param fileName
 * @param extension
 * @param url
 * @param processedFiles
 * @param ydistance_percent
 * @param yoffset_percent
 * @param xoffset_percent
 * @param xdistance_percent
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class Photo(
  @(JsonProperty@field)("id")
  id: String,

  @(JsonProperty@field)("url")
  url: String,

  @(JsonProperty@field)("main")
  main: Option[Any],

  @(JsonProperty@field)("fileName")
  fileName: String,

  @(JsonProperty@field)("extension")
  extension: String,

  @(JsonProperty@field)("processedFiles")
  processedFiles: List[ProcessedFile],

  @(JsonProperty@field)("shape")
  shape: Option[String],

  @(JsonProperty@field)("crop")
  crop: Option[String],

  @(JsonProperty@field)("ydistance_percent")
  ydistance_percent: Option[Double],

  @(JsonProperty@field)("yoffset_percent")
  yoffset_percent: Option[Double],

  @(JsonProperty@field)("xoffset_percent")
  xoffset_percent: Option[Double],

  @(JsonProperty@field)("xdistance_percent")
  xdistance_percent: Option[Double]
)
