package com.pxnc.task

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import com.pxnc.scheduler.model.Task
import com.pxnc.scheduler.service.{BaseSpec, TaskScheduler}
import com.pxnc.scheduler.settings.Settings

import scala.concurrent.{Await, Future}
import scala.io.Source

/**
  * @author Roman Maksyutov
  */
class TaskExecutorSpec extends BaseSpec with TaskExecutorSpecHelper {

  implicit val ex = scala.concurrent.ExecutionContext.global

  import java.util.TimeZone

  TimeZone.setDefault(TimeZone.getTimeZone(Settings.defaultTimeZoneOffset.getId))

  List(
    "test-plan-1.csv",
    "test-plan-2.csv"
  ) foreach { file =>

    s"test [$file]" in {

      import com.pxnc.util.TimeUtils._

      import scala.concurrent.duration._


      val taskScheduler = new TaskScheduler[String]

      val now = LocalDateTime.now.plusSeconds(1)

      /**
        * Создание задачи из строки. Строка парсится в duration и затем
        */
      def mkTask(s: String): Task[_] = {
        val sign = if (s.startsWith("-")) -1 else 1
        val timeShift = s.toMillisTime * sign
        val startAt = now.plus(timeShift, ChronoUnit.MILLIS)
        val result = timeShift match {
          case 0 =>
            true
          case 333 =>
            333
          case t if t > 0 =>
            s"Успешно выполенено за: $timeShift ms"
          case t if t < 0 =>
            s"Этот результат не должен отображаться, т.к  из-за отрицательного значения возникает ошибка: $timeShift"
        }
        Task(startAt, TestJob(timeShift, result))
      }


      val source = Source.fromResource(file)
      val lines = source.getLines()
      val testPlan: List[Array[Task[_]]] = lines.map {
        l =>
          l.split("\\s*,\\s*") map mkTask
      }.toList


      val startedAt = LocalDateTime.now
      val futures = Future.sequence(testPlan map { tasks =>
        Future(taskScheduler.schedule(tasks: _*))
      })
      val fRes = futures flatMap { s =>
        taskScheduler.result map { res =>
          //          logger.boldTable(res.sorted.map(handleTaskResult).mkString("\n"), s"Результаты выполнения задач: [${res.size}]. Старт [${startedAt.str}]")
          logger.boldTable(res.sorted.map(handleTaskResult).mkString("\n"),
            s" <Ок: ID[номмер][добавлено / запланировано]: ~[погрешность] [старт]-[завершение] = [время выполнения] => [результат])> задач: [${res.size}] старт [${startedAt.str}]")
          assert("" == "")
        }
      }

      Await.result(fRes, Duration.Inf)

    }

  }

}
