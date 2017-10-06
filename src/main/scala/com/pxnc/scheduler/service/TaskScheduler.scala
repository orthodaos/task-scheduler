package com.pxnc.scheduler.service

import com.pxnc.scheduler.model.{Task, TaskResult}
import com.pxnc.util.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * @author Roman Maksyutov
  */
class TaskScheduler[T](implicit handleResult: Try[_] => T, ec: ExecutionContext) extends Logging {

  val defaultTaskExecutor: TaskExecutor[T] = new TaskExecutor[T]


  def schedule(tasks: Task[_]*): Unit = {
    tasks foreach defaultTaskExecutor.submit
  }


  def result: Future[List[TaskResult[T]]] =
    defaultTaskExecutor.completeAndGet()


}
