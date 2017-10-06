package com.pxnc.scheduler.model

import java.time.LocalDateTime
import java.util.concurrent.Callable

/**
  * Исходный вид задачи, в котором она поступает извне.
  *
  * @author Roman Maksyutov
  * @param startAt время в миллисекнудах UTC
  * @param job
  * @tparam T
  */
case class Task[T](startAt: LocalDateTime, job: Callable[T])