package com.pxnc.scheduler.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
  * Результат выполенения задачи
  *
  * @author Roman Maksyutov
  */
case class TaskResult[R] private(
                                  result: R,
                                  startedAt: Option[LocalDateTime],
                                  task: Option[ScheduledTask[_]] = None,
                                  completedAt: LocalDateTime = LocalDateTime.now,
                                ) {

  /**
    * @return Время выполнения задания
    */
  def executionTimeMills: Long = {
    startedAt map {
      _.until(completedAt, ChronoUnit.MILLIS)
    } getOrElse 0
  }

}


object TaskResult {

  def apply[R](result: R, startedAt: LocalDateTime, task: ScheduledTask[_], completedAt: LocalDateTime): TaskResult[R] =
    new TaskResult[R](result, Some(startedAt), Some(task), completedAt)


  def apply[R](result: R, startedAt: LocalDateTime, task: ScheduledTask[_]): TaskResult[R] =
    new TaskResult[R](result, Some(startedAt), Some(task))


  def apply[R](result: R, task: ScheduledTask[_]): TaskResult[R] =
    new TaskResult(result, None, Some(task))


  def apply[R](result: R): TaskResult[R] = {
    val now = LocalDateTime.now
    new TaskResult(result, Some(now), None, now)
  }

}
