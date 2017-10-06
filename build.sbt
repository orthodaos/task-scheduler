
name := "task-scheduler"

organization := "pxnc.com"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
//libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.2.3"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "com.typesafe" % "config" % "1.3.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"