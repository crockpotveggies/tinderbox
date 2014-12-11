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
  val id: String,

  @(JsonProperty@field)("url")
  val url: String,

  @(JsonProperty@field)("main")
  val main: Option[Any],

  @(JsonProperty@field)("fileName")
  val fileName: String,

  @(JsonProperty@field)("extension")
  val extension: String,

  @(JsonProperty@field)("processedFiles")
  val processedFiles: List[ProcessedFile],

  @(JsonProperty@field)("shape")
  val shape: Option[String],

  @(JsonProperty@field)("crop")
  val crop: Option[String],

  @(JsonProperty@field)("ydistance_percent")
  val ydistance_percent: Option[Double],

  @(JsonProperty@field)("yoffset_percent")
  val yoffset_percent: Option[Double],

  @(JsonProperty@field)("xoffset_percent")
  val xoffset_percent: Option[Double],

  @(JsonProperty@field)("xdistance_percent")
  val xdistance_percent: Option[Double]
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("","",None,"","",List(new ProcessedFile()),None,None,None,None,None,None)
}
