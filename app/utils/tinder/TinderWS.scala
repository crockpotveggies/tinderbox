package play.api.libs.ws.tinder

import scala.concurrent.{ Future, Promise }
import play.api.http.{ Writeable, ContentTypeOf }
import play.api.libs.ws.WS._
import play.api.libs.ws.{SignatureCalculator, Response}
import com.ning.http.client.Realm.AuthScheme
import com.ning.http.client.PerRequestConfig


class TinderRequest(
  url: String,
  headers: Map[String, Seq[String]],
  queryString: Map[String, Seq[String]],
  calc: Option[SignatureCalculator],
  auth: Option[Tuple3[String, String, AuthScheme]],
  followRedirects: Option[Boolean],
  requestTimeout: Option[Int],
  virtualHost: Option[String]

) extends WSRequestHolder(url,headers,queryString,calc,auth,followRedirects,requestTimeout,virtualHost) {

  /**
   * adds any number of HTTP headers
   * @param hdrs
   */
  override def withHeaders(hdrs: (String, String)*): TinderRequest = {
    val headers = hdrs.foldLeft(this.headers)((m, hdr) =>
      if (m.contains(hdr._1)) m.updated(hdr._1, m(hdr._1) :+ hdr._2)
      else m + (hdr._1 -> Seq(hdr._2))
    )
    new TinderRequest(this.url, headers, this.queryString, this.calc, this.auth, this.followRedirects, this.requestTimeout, this.virtualHost)
  }

  /**
   * Perform a GET on the request asynchronously with a body.
   */
  def get[T](body: T)(implicit wrt: Writeable[T], ct: ContentTypeOf[T]): Future[Response] = prepare("GET", body).execute

  /**
   * Prepare a request
   * @param method
   * @param body
   * @param wrt
   * @param ct
   * @tparam T
   * @return
   */
  override def prepare[T](method: String, body: T)(implicit wrt: Writeable[T], ct: ContentTypeOf[T]) = {
    val request = new WSRequest(method, auth, calc).setUrl(url)
      .setHeaders(Map("Content-Type" -> Seq(ct.mimeType.getOrElse("text/plain"))) ++ headers)
      .setQueryString(queryString)
      .setBody(wrt.transform(body))
    followRedirects.map(request.setFollowRedirects)
    requestTimeout.map {
      t: Int =>
        val config = new PerRequestConfig()
        config.setRequestTimeoutInMs(t)
        request.setPerRequestConfig(config)
    }
    virtualHost.map {
      v =>
        request.setVirtualHost(v)
    }
    request
  }

}

object TinderWS {
  /**
   * Prepare a new request. You can then construct it by chaining calls.
   *
   * @param url the URL to request
   */
  def url(url: String): TinderRequest = new TinderRequest(url, Map(), Map(), None, None, None, None, None)
}
