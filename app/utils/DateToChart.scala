package utils

import java.text.SimpleDateFormat
import java.util.{TimeZone, Date}
import org.joda.time._
import org.joda.time.format._


/**
 * Helper to convert ISO dates to bucketed data for front-end charts.
 */
object DateToChart {

  /**
   * Bucket a list of string ISO8601 dates by sequential days. Buckets will begin at the first day and end at the last.
   *
   * @note If there are any missing dates, this function will return a zero for that day.
   * @param dateStrings
   * @param timezoneOffset
   * @return
   */
  def isoDailyBuckets(dateStrings: List[String], timezoneOffset: Int=0): Array[Int] = {
    if(timezoneOffset > 24) throw new java.lang.NumberFormatException("Invalid timezone offset given, must be in hours: "+timezoneOffset)

    val dates =
      dateStrings
      .map { s =>
        val dt = ISO8601Parser.parseDateTime(s)
        dt.withZone(DateTimeZone.forOffsetHours(timezoneOffset))
        Days.daysBetween(zeroDate, dt).getDays
      }
      .sortBy( d => d )

    val lowest = dates.head
    val highest = dates.last
    val length = highest - lowest

    val groups = dates.groupBy( d => d ).toArray

    val buckets = new Array[Int](length)
    var i = 0
    while (i < length) {
      buckets(i) = groups(i)._2.size
      i += 1
    }
    buckets
  }

  /**
   * Bucket a list of string ISO8601 into the 24 hours in a day.
   *
   * @note If there are any missing times, this function will return a zero for that time.
   * @param dateStrings
   * @param timezoneOffset
   * @return
   */
  def isoHourBuckets(dateStrings: List[String], timezoneOffset: Int=0): Array[Int] = {
    if(timezoneOffset > 24) throw new java.lang.NumberFormatException("Invalid timezone offset given, must be in hours: "+timezoneOffset)

    val times =
      dateStrings
      .map { s =>
        val dt = ISO8601Parser.parseDateTime(s)
        dt.withZone(DateTimeZone.forOffsetHours(timezoneOffset))
        dt.toString("HH")
      }
      .map(_.toInt)
      .sortBy( d => d )

    val length = 24

    val groups = times.groupBy( d => d ).toArray

    val buckets = new Array[Int](length)
    var i = 0
    while (i < length) {
      buckets(i) = groups(i)._2.size
      i += 1
    }
    buckets
  }

  /**
   * Helper method for instantiating the ISO date parser.
   * @return
   */
  private def ISO8601Parser = ISODateTimeFormat.dateTimeParser()


  /**
   * Helper method for instantiating the ISO date parser.
   * @return
   */
  private def zeroDate = new DateTime(0L)

}
