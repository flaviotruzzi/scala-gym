name := "scala-gym"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.6"

libraryDependencies += "com.typesafe.akka" % "akka-http-core_2.11" % "2.4.6"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.6"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.7"

libraryDependencies += "com.spotify" % "docker-client" % "5.0.1"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0-RC2"
