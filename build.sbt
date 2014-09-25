name := "common-config"

organization := "com.blinkbox.books"

version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0")

crossScalaVersions := Seq("2.10.4", "2.11.2")

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")

libraryDependencies ++= {
  Seq(
    "com.typesafe"               %  "config"              % "1.2.1",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
    "ch.qos.logback"             %  "logback-classic"     % "1.1.2",
    "org.json4s"                 %% "json4s-jackson"      % "3.2.10",
    "com.blinkbox.books"         %% "common-scala-test"   % "0.3.0"   % Test
  )
}
