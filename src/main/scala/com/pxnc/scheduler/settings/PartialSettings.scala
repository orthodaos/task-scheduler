package com.pxnc.scheduler.settings

import java.time.ZoneOffset

import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

/**
  * @author Roman Maksyutov
  */
class PartialSettings private[settings](config: Config) {

  import com.pxnc.util.TimeUtils.TimeParser

  implicit def path2ZoneOffset(path: String): ZoneOffset = path.zoneOffset

  implicit def path2Int(path: String): Int = path.int


  implicit class PathExt(path: String) {

    def str: String = config.getString(path)

    def int: Int = config.getInt(path)

    def duration: FiniteDuration = path.str.toDuration

    def zoneOffset: ZoneOffset = ZoneOffset.of(path.str)
  }


}