package utils.tinder.model

import com.fasterxml.jackson.annotation._

import scala.annotation.meta.field


/**
 * A photo in a user's instagram profile.
 * @param image
 * @param thumbnail
 * @param ts
 * @param link
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class InstagramPhoto(
  @(JsonProperty@field)("image")
  image: String,

  @(JsonProperty@field)("thumbnail")
  thumbnail: String,

  @(JsonProperty@field)("ts")
  ts: String,

  @(JsonProperty@field)("link")
  link: String
)
