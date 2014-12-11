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
  val token: String,

  @(JsonProperty@field)("globals")
  val globals: TinderGlobals,

  @(JsonProperty@field)("user")
  val user: User,

  @(JsonProperty@field)("versions")
  val versions: TinderVersion
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("",new TinderGlobals(),new User(),new TinderVersion())
}
