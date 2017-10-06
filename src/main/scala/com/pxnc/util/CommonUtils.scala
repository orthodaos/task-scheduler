package com.pxnc.util

/**
  * @author Roman Maksyutov
  */
object CommonUtils {

  implicit class OptionComparableExt[T](o: Option[T]) {

    def compareWith[R <: Comparable[R]]: (Option[T]) => Int = o2 => (o, o2) match {

      case (Some(x: R), Some(y: R)) =>
        x.compareTo(y)

      case (Some(_), None) =>
        -1
      case (None, Some(_)) =>
        1
      case (None, None) =>
        0
    }

  }

}
