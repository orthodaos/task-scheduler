package com.pxnc.task

import java.time.temporal.ChronoUnit
import java.util.concurrent.Callable

import com.pxnc.exception.TaskProcessingException
import com.pxnc.scheduler.model.TaskResult

import scala.util.{Failure, Success, Try}

/**
  * @author Roman Maksyutov
  */
trait TaskExecutorSpecHelper {
  self: TaskExecutorSpec =>


  /**
    * Сортировка задач для отображения результата
    */
  implicit object StringTaskResultOrder extends Ordering[TaskResult[String]] {

    override def compare(x: TaskResult[String], y: TaskResult[String]): Int = {

      import com.pxnc.util.CommonUtils._
      val cmp: Int = x.startedAt.compareWith(y.startedAt)
      if (cmp == 0) {
        x.task.compareWith(y.task)
      } else {
        cmp
      }
    }

  }


  implicit val ExecutedResultHandler: Try[_] => String = {

    case Success(r: Int) =>
      s"Число($r)"

    case Success(s: String) =>
      s"Строка($s)"

    case Success(s) =>
      s"Неизвестно($s)"

    case Failure(e: TaskProcessingException) =>
      e.getLocalizedMessage

    case Failure(e) =>
      s"Непредвиденная ошибка: [$e]"

  }


  case class TestJob[R](time: Long, result: R) extends Callable[R] {
    override def call(): R = {
      try {
        Thread.sleep(time)
      } catch {
        case _: Throwable =>
          throw TaskProcessingException(time)
      }
      if (time == 666) {
        throw new IllegalArgumentException("Запланированная ошибка 666")
      }
      result
    }
  }

  /**
    * Формирования отчета о выполенении задачи
    *
    * @param tr
    * @tparam T
    * @return
    */
  implicit def handleTaskResult[T](tr: TaskResult[T]): String = {
    import com.pxnc.util.TimeUtils._

    tr.task map { t =>
      val raw = t.rawTask

      val dif = tr.startedAt.map { startedAt =>
        raw.startAt.until(startedAt, ChronoUnit.MILLIS) + "ms"
      } getOrElse "-"
      //      s"Ок: ID[${t.id}][${t.submitTime.str}], запланирована:[${raw.startAt.str}], старт:[${tr.startedAt.str}], погрешность: [$dif], завершение:[${tr.completedAt.str}], выполенение: [${tr.executionTimeMills}ms] => [${tr.result}])"
      s"Ок: ID[${t.id}][${t.submitTime.str}-${raw.startAt.str}]: ~[$dif] [${tr.startedAt.str}]-[${tr.completedAt.str}] = [${tr.executionTimeMills}ms] => [${tr.result}])"
    } getOrElse {
      val res = s"Ошибка отсутствует задача! '_': старт:[${tr.startedAt.str}], запланирована:[-], завершение:[${tr.completedAt.str}]"
      logger.error(res)
      res
    }
  }


}
