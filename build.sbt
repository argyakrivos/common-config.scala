lazy val root = (project in file(".")).
  settings(
    name := "common-config",
    organization := "com.blinkbox.books",
    version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.11.4"),
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7", "-Xfatal-warnings", "-Xfuture"),
    libraryDependencies ++= Seq(
      "com.typesafe"               %  "config"            % "1.2.1",
      "com.typesafe.scala-logging" %% "scala-logging"     % "3.1.0",
      "ch.qos.logback"             %  "logback-classic"   % "1.1.2",
      "org.json4s"                 %% "json4s-jackson"    % "3.2.11",
      "com.blinkbox.books"         %% "common-scala-test" % "0.3.0"   % Test
    )
  )

