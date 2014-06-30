package com.blinkbox.books.logging.gelf

import ch.qos.logback.classic.{LoggerContext, PatternLayout}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.util.LevelToSyslogSeverity
import ch.qos.logback.core.{CoreConstants, LayoutBase}
import java.io.StringWriter
import java.net.{InetAddress, NetworkInterface, UnknownHostException}
import java.text.{NumberFormat, ParsePosition}
import org.json4s.JsonAST.{JString, JDecimal}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import scala.beans.BeanProperty
import scala.collection.JavaConverters._

private object NumberChecker {
  private val numberFormat = new ThreadLocal[NumberFormat] {
    override def initialValue() = NumberFormat.getNumberInstance
  }
  def isNumeric(s: String) = {
    val pp = new ParsePosition(0)
    numberFormat.get.parse(s, pp)
    pp.getIndex == s.length && pp.getErrorIndex == -1
  }
}

class GelfLayout extends LayoutBase[ILoggingEvent] {
  import NumberChecker._

  private var shortMessageLayout: PatternLayout = null
  private var fullMessageLayout: PatternLayout = null
  private var exceptionLayout: PatternLayout = null
  private var hostName: String = null

  @BeanProperty var facility: String = null
  @BeanProperty var shortMessagePattern: String = "%.-128message%nopex"
  @BeanProperty var fullMessagePattern: String = "%message%nopex"
  @BeanProperty var includeLoggerName: Boolean = false
  @BeanProperty var includeThreadName: Boolean = false

  override def start() {
    shortMessageLayout = newPatternLayout(shortMessagePattern)
    fullMessageLayout = newPatternLayout(fullMessagePattern)
    exceptionLayout = newPatternLayout("%xThrowable")
    hostName = lookupHostName
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
      ("host" -> hostName) ~
      ("short_message" -> shortMessageLayout.doLayout(event)) ~
      ("full_message" -> fullMessageLayout.doLayout(event)) ~
      ("timestamp" -> JDecimal(BigDecimal(event.getTimeStamp) / 1000)) ~
      ("level" -> LevelToSyslogSeverity.convert(event))

    if (facility != null) json ~= ("_facility", facility)
    if (includeLoggerName) json ~= ("_loggerName", event.getLoggerName)
    if (includeThreadName) json ~= ("_threadName", event.getThreadName)
    if (event.getMarker != null) json ~= ("_marker", event.getMarker.toString)
    callerData(event).foreach(json ~= _)
    event.getMDCPropertyMap.asScala.foreach {
      // graylog only allows functions over numeric values so if this string looks like a number
      // then turn it into one. unfortunately the original type information has been lost by now.
      case (k, v) => json ~= (s"_$k", if (isNumeric(v)) JDecimal(BigDecimal(v)) else JString(v))
    }
    exceptionData(event).foreach(json ~= _)

    val layout = new StringWriter(512)
    mapper.writeValue(layout, render(json))
    layout.write(CoreConstants.LINE_SEPARATOR)
    layout.toString
  }

  private def lookupHostName: String =
    try InetAddress.getLocalHost.getHostName
    catch {
      case e: UnknownHostException =>
        addWarn("Failed to get host name; trying to find a non-loopback address", e)
        val addresses = for {
          interface <- NetworkInterface.getNetworkInterfaces.asScala
          address <- interface.getInetAddresses.asScala if !address.isLoopbackAddress
        } yield address
        addresses.take(1).toList match {
          case address :: _ => address.getHostAddress
          case _ => addError("Failed to get any value for host name"); "unknown"
        }
    }

  private def newPatternLayout(pattern: String) = {
    val layout = new PatternLayout
    layout.setContext(new LoggerContext)
    layout.setPattern(pattern)
    layout.start()
    layout
  }

  private def callerData(event: ILoggingEvent) =
    event.getCallerData match {
      case Array(c, _*) => Some(("_file" -> c.getFileName) ~ ("_line" -> c.getLineNumber) ~ ("_method" -> c.getMethodName))
      case _ => None
    }

  private def exceptionData(event: ILoggingEvent) =
    for (proxy <- Option(event.getThrowableProxy)) yield {
      ("_exceptionClass" -> proxy.getClassName) ~
      ("_exceptionMessage" -> proxy.getMessage) ~
      ("_exceptionDetail" -> exceptionLayout.doLayout(event))
    }
}
