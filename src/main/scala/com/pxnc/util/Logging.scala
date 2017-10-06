package com.pxnc.util

import com.pxnc.util.AdvancedTableBuilder.{BoldBorder, DoubleBorder, LightBorder}
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author Roman Maksyutov
  */
trait Logging {
  self =>

  val logger: Logger = LoggerFactory getLogger getClass

  //TODO  make logger on streams in future (_log = akka.event.Logging(context.system, this))
  //  val log: LoggingAdapter = Logging(ApplicationContext.system, self)

  implicit class LoggerExt(logger: org.slf4j.Logger) {

    def info(obj: Any) = logger.info("{}", obj)

    def debug(obj: Any) = logger.debug("{}", obj)

    def warn(obj: Any) = logger.warn("{}", obj)

    def error(obj: Any) = logger.error("{}", obj)

    def table[T](obj: T): Unit = table(obj, None)

    def table[T](obj: T, title: String): Unit = table(obj, Some(title))

    def table[T](obj: T, title: String, alignment: TableAlignment): Unit = table(obj, Some(title), alignment)

    def table[T](obj: T, title: Option[String], alignment: TableAlignment = TableAlignments.Left): Unit =
      logger.info("{}",
        new AdvancedTableBuilder(
          title,
          obj,
          alignment, LightBorder
        )
      )


    def boldTable[T](obj: T): Unit = boldTable(obj, None)

    def boldTable[T](obj: T, title: String): Unit = boldTable(obj, Some(title))

    def boldTable[T](obj: T, title: String, alignment: TableAlignment): Unit = boldTable(obj, Some(title), alignment)

    def boldTable[T](obj: T, title: Option[String], alignment: TableAlignment = TableAlignments.Left): Unit =
      logger.info("{}",
        new AdvancedTableBuilder(
          title,
          obj,
          alignment, BoldBorder
        )
      )

    def doubleTable[T](obj: T): Unit = doubleTable(obj, None)

    def doubleTable[T](obj: T, title: String): Unit = doubleTable(obj, Some(title))

    def doubleTable[T](obj: T, title: String, alignment: TableAlignment): Unit = doubleTable(obj, Some(title), alignment)

    def doubleTable[T](obj: T, title: Option[String], alignment: TableAlignment = TableAlignments.Left): Unit =
      logger.info("{}",
        new AdvancedTableBuilder(
          title,
          obj,
          alignment, DoubleBorder
        )
      )

  }

}
