package com.pxnc.scheduler.service

import java.time.LocalDateTime
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}
import java.util.concurrent.{PriorityBlockingQueue, TimeUnit}

import com.pxnc.scheduler.model.{ScheduledTask, Task, TaskResult}
import com.pxnc.util.Logging

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * @author Roman Maksyutov
  */
private[service] class TaskExecutor[T](implicit handleResult: Try[_] => T, ec: ExecutionContext) extends Logging {

  /**
    *
    * Очередь с раписанием задач.
    * т.к. в условии не указаны дополнительные сведения о добавлении задач,
    * как предполагаемые  частота, кол-во и т.п.,
    * то используем стандартную блокирующую очередь с сортировкой
    **/
  private val queue: PriorityBlockingQueue[ScheduledTask[_]] = new PriorityBlockingQueue[ScheduledTask[_]](500)


  /**
    * Флаг определяющий что загрузка данных завершена и необходимо
    * завершить основной поток после выполнения всех оставшихся задач
    * (для удобства тестирования)
    */
  private val returnResult = new AtomicBoolean(false)


  /**
    * Миллисекуд до старта следующей задачи
    */
  private val lastDelay = new AtomicLong(Long.MaxValue)


  type TR = TaskResult[T]


  /**
    * Добавление в очередь задачи.
    * Возвзращает true в случае если задача проставлена на ближайшее время
    * и необходимо уведомить поток расписания
    *
    * @param task
    * @return
    */
  def submit(task: Task[_]): Unit = {
    val scheduled = ScheduledTask(task)
    queue.offer(scheduled)

    val last = lastDelay.get()
    val delay = scheduled.currentDelay()
    if (last > delay && this.lastDelay.compareAndSet(last, delay)) {
      // уведомляем только если добавили задачу в начало очереди
      syncNotify()
    }
  }


  private val result: Future[List[TaskResult[T]]] =
    Future(execute()).flatten


  def completeAndGet(): Future[List[TaskResult[T]]] = {
    returnResult.set(true)
    synchronized(notifyAll())
    result
  }


  private def syncWait(timeout: Long = 0): Unit = {
    synchronized(wait(timeout.max(0)))
  }


  private def syncNotify(): Unit = {
    synchronized(notify())
  }


  @tailrec
  private def nextTask(): Option[ScheduledTask[_]] = {
    Option(queue.poll(0, TimeUnit.NANOSECONDS)) match {

      case None =>
        None

      case someTask@Some(task) =>
        if (task.currentDelay() > 0) {
          lastDelay.set(task.currentDelay())
          syncWait(task.currentDelay())

          if (task.currentDelay > 0) {
            // вышли из ожидания раньше срока - происходит только при добавлении более ранней задачи
            queue.offer(task)
            nextTask()
          } else {
            someTask
          }
        } else {
          someTask
        }
    }
  }


  /**
    * Запуск обработки задачи и возвращение резуьтата
    *
    * @param task
    * @return
    */
  protected def processingTask(task: ScheduledTask[_]): Future[TaskResult[T]] = {
    val startedAt = LocalDateTime.now
    Future {
      val result: T = task.tryExecute
      val completed = LocalDateTime.now
      TaskResult(result, startedAt, task, completed)
    } transform {
      case Success(r) =>
        Success(r)
      case f@Failure(_) =>
        Success(TaskResult[T](f, task))
    }
  }


  /**
    * Работа с очередью задач
    *
    * @param processing задачи впроцессе (для удобства тестирования)
    * @return
    */
  @tailrec private def execute(processing: List[Future[TaskResult[T]]] = Nil): Future[List[TaskResult[T]]] = {

    nextTask() match {

      case None if returnResult.get() =>
        // завершение работы
        Future.sequence(processing)

      case None =>
        syncWait()
        execute(processing)

      case Some(task) =>
        execute(processingTask(task) :: processing)
    }
  }
}