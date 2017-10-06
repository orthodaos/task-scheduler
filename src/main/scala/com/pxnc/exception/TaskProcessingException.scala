package com.pxnc.exception

/**
  * @author Roman Maksyutov
  */
case class TaskProcessingException(time: Long) extends RuntimeException(s"Ошибка выполнения задачи (${time}ms)")