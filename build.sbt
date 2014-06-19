name := "common-config"

organization := "com.blinkbox.books"

version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0")

scalaVersion  := "2.10.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")

libraryDependencies ++= {
  val json4sV = "3.2.10"
  Seq(
    "com.typesafe"        %   "config"          % "1.2.1",
    "ch.qos.logback"      %   "logback-classic" % "1.1.2",
    "org.json4s"          %%  "json4s-jackson"  % json4sV,
    "org.scalatest"       %%  "scalatest"       % "2.1.6" % "test",
    "junit"               %   "junit"           % "4.11"  % "test",
    "com.novocode"        %   "junit-interface" % "0.10"  % "test"
  )
}
