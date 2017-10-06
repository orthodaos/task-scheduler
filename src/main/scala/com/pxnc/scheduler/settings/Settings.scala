package com.pxnc.scheduler.settings

import java.time.ZoneOffset

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Конфигурационные настройки
  *
  * @author Roman Maksyutov
  */
class Settings private(config: Config) extends PartialSettings(config)

object Settings extends Settings(ConfigFactory.load()) {

  implicit val defaultTimeZoneOffset: ZoneOffset = Option[ZoneOffset]("time-zone-offset").getOrElse(ZoneOffset.UTC)

  val OutputTimeFormat: String = "output-time-format".str

  val MinTableLength: Int = "table-length-min"

  val MaxTableLength: Int = "table-length-max"

}
