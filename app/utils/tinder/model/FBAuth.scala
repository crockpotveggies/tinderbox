package utils.tinder.model

import scala.annotation.meta.field
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation._


/**
 * Facebook creds object for authenticating with Tinder.
 * @param facebook_token
 * @param facebook_id
 */
@JsonIgnoreProperties(ignoreUnknown=true)
case class FBAuth(
  @(JsonProperty@field)("facebook_token")
  facebook_token: String,

  @(JsonProperty@field)("facebook_id")
  facebook_id: String
) {
  /**
   * necessary for object instantiation
   */
  def this() = this("","")
}
