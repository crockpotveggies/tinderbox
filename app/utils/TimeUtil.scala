package utils

import java.text.SimpleDateFormat
import java.util.{TimeZone, Date}


/**
 * Generic utilities for dealing with time.
 */
object TimeUtil {

  def parseISO8601(time: String): Date = Format.ISO8601.parse(time)

  object Format {
    def ISO8601 = {
      val f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      f.setTimeZone(TimeZone.getTimeZone("UTC"))
      f
    }
  }
}
