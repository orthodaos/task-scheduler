package com.pxnc.scheduler.model


import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.atomic.AtomicLong

import com.pxnc.util.Logging

import scala.util.Try

/**
  * Задача поставленая в очередь расписания
  *
  * @param id            ID задачи
  * @param startAtMillis время планируемого старта в миллисекундах (для ускорения при сортировке и др. вычислений)
  * @param rawTask       исходные данные по задаче
  * @param submitTime    время создания запланированной задачи (добавления в очередь)
  * @tparam T
  * @author Roman Maksyutov
  */
case class ScheduledTask[T] private(
                                     id: Long,
                                     startAtMillis: Long,
                                     rawTask: Task[T],
                                     submitTime: LocalDateTime = LocalDateTime.now
                                   ) extends Ordered[ScheduledTask[T]] {

  /**
    * Сортировка для планируемых задач
    *
    * @param o
    * @return
    */
  override def compare(o: ScheduledTask[T]): Int = {
    val res = java.lang.Long.compare(startAtMillis, o.startAtMillis)
    if (res == 0) java.lang.Long.compare(id, o.id) else res
  }


  /**
    * выполнение задачи и оборачинвание результата в Try блок
    *
    * @return
    */
  def tryExecute = Try(rawTask.job.call)


  /**
    * Вычисление оставшегося времени до выполнения задачи
    *
    * @return
    */
  def currentDelay(): Long = startAtMillis - System.currentTimeMillis()

}


object ScheduledTask extends Logging {

  /**
    * сиквенс ID для натуральной приоритизации задач с одинаковым временем старта
    */
  private val sequence: AtomicLong = new AtomicLong(0)

  def apply[T](task: Task[T]): ScheduledTask[T] = {

    val millis = task.startAt.toInstant(ZoneOffset.UTC).toEpochMilli
    new ScheduledTask[T](sequence.getAndIncrement(), millis, task)
  }


}