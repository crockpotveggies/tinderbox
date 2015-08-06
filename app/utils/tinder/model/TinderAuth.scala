package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * A response when authenticating containing info about the current token.
 * @param token
 * @param globals
 * @param user
 * @param versions
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class TinderAuth(
  @(JsonProperty@field)("token")
  token: String,

  @(JsonProperty@field)("globals")
  globals: TinderGlobals,

  @(JsonProperty@field)("user")
  user: User,

  @(JsonProperty@field)("versions")
  versions: TinderVersion
)
