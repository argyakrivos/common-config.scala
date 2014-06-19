package com.blinkbox.books.logging.gelf

import ch.qos.logback.classic.{LoggerContext, PatternLayout}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.util.LevelToSyslogSeverity
import ch.qos.logback.core.{CoreConstants, LayoutBase}
import java.io.StringWriter
import java.net.InetAddress
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class GelfLayout extends LayoutBase[ILoggingEvent] {
  private var shortMessageLayout: PatternLayout = null
  private var fullMessageLayout: PatternLayout = null
  private var exceptionLayout: PatternLayout = null

  @BeanProperty var facility: String = null
  @BeanProperty var shortMessagePattern: String = "%.-128message%nopex"
  @BeanProperty var fullMessagePattern: String = "%message%nopex"
  @BeanProperty var includeLoggerName: Boolean = false
  @BeanProperty var includeThreadName: Boolean = false

  override def start() {
    shortMessageLayout = newPatternLayout(shortMessagePattern)
    fullMessageLayout = newPatternLayout(fullMessagePattern)
    exceptionLayout = newPatternLayout("%xThrowable")
    super.start()
  }

  override def stop() {
    shortMessageLayout.stop()
    fullMessageLayout.stop()
    exceptionLayout.stop()
  }

  override def doLayout(event: ILoggingEvent): String = {
    var json =
      ("version" -> "1.1") ~
        ("host" -> InetAddress.getLocalHost.getHostName) ~
        ("short_message" -> shortMessageLayout.doLayout(event)) ~
        ("full_message" -> fullMessageLayout.doLayout(event)) ~
        ("timestamp" -> event.getTimeStamp / 1000.0) ~
        ("level" -> LevelToSyslogSeverity.convert(event))

    if (facility != null) json ~= ("_facility", facility)
    if (includeLoggerName) json ~= ("_loggerName", event.getLoggerName)
    if (includeThreadName) json ~= ("_threadName", event.getThreadName)
    if (event.getMarker != null) json ~= ("_marker", event.getMarker.toString)
    callerData(event).foreach(json ~= _)
    event.getMDCPropertyMap.asScala.foreach { case (k, v) => json ~= (s"_$k", v) }
    exceptionData(event).foreach(json ~= _)

    val layout = new StringWriter(512)
    mapper.writeValue(layout, render(json))
    layout.write(CoreConstants.LINE_SEPARATOR)
    layout.toString
  }

  private def newPatternLayout(pattern: String) = {
    val layout = new PatternLayout
    layout.setContext(new LoggerContext)
    layout.setPattern(pattern)
    layout.start()
    layout
  }

  private def callerData(event: ILoggingEvent): Option[JObject] =
    event.getCallerData match {
      case Array(c, _*) => Some(("_file" -> c.getFileName) ~ ("_line" -> c.getLineNumber) ~ ("_method" -> c.getMethodName))
      case _ => None
    }

  private def exceptionData(event: ILoggingEvent): Option[JObject] =
    Option(event.getThrowableProxy) map { proxy =>
      ("_exceptionClass" -> proxy.getClassName) ~
        ("_exceptionMessage" -> proxy.getMessage) ~
        ("_exceptionDetail" -> exceptionLayout.doLayout(event))
    }
}
