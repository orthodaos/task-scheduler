package com.pxnc.util

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

import com.pxnc.scheduler.settings.Settings

import scala.annotation.tailrec
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.Try
import scala.util.matching.Regex.Match

/**
  * @author Roman Maksyutov
  */
object TimeUtils {

  val DTF: DateTimeFormatter = DateTimeFormatter.ofPattern(Settings.OutputTimeFormat)

  private val TimeRegex = "(\\d+)\\s*(\\w*)".r


  implicit class IntDurationExt(i: Int) {

    import scala.concurrent.duration.DurationInt

    def ms = i millis

    def s = i second
  }


  implicit class FiniteDurationExt(d: FiniteDuration) {

    def apply(i: FiniteDuration): FiniteDuration = d + i

  }


  implicit class TimeParser(str: String) {

    /**
      * Convert text like '100 ms' or '100 sec' to time in milliseconds.
      * If the time unit is not specified then is taken milliseconds by default
      */
    private def toTime(f: TimeUnit => Long => Long): Long = Try {

      def parseTimeUnit: String => TimeUnit = {
        case "ms" | "milli" | "millis" | "millisecond" | "milliseconds" | "" => TimeUnit.MILLISECONDS
        case "s" | "sec" | "second" | "seconds" => TimeUnit.SECONDS
        case "m" | "min" | "minute" | "minutes" => TimeUnit.MINUTES
        case "h" | "hour" | "hours" => TimeUnit.HOURS
        case "d" | "day" | "days" => TimeUnit.DAYS
      }

      @tailrec def parseTime(matches: List[Match], time: Long): Long = matches match {
        case Nil =>
          time
        case head :: tail =>
          parseTime(tail, time + f(parseTimeUnit(head.group(2)))(head.group(1).toLong))
      }

      parseTime(TimeRegex.findAllMatchIn(str).toList, 0)

    } getOrElse (throw new IllegalArgumentException(s"Wrong time format for: '$str]'"))


    def toMillisTime: Long = toTime(_.toMillis)

    def toNanosTime: Long = toTime(_.toNanos)

    def toDuration: FiniteDuration = toMillisTime millis

    def toDateTime(implicit offset: ZoneOffset): LocalDateTime = {
      val nanos = TimeUnit.MILLISECONDS.toNanos(toMillisTime)
      val seconds = TimeUnit.NANOSECONDS.toSeconds(nanos)
      val nanosPerSecond: Int = (TimeUnit.SECONDS.toNanos(seconds) - nanos).toInt

      LocalDateTime.ofEpochSecond(seconds, nanosPerSecond, offset)
    }

  }


  implicit class LocalDateTimeExt(t: LocalDateTime) {

    def str: String =
      t.format(DTF)


    //    def withOffset: LocalDateTime =
    //      t.atOffset(Settings.timeZoneOffset).toLocalDateTime


  }

  implicit class OptionLocalDateTimeExt(ot: Option[LocalDateTime]) {

    def str: String = ot.map(_.str) getOrElse "-"

  }

}
