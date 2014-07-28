package com.blinkbox.books.logging.gelf

import java.net.InetAddress

import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.classic.{Level, LoggerContext}
import org.json4s.JsonAST.{JDecimal, JInt}
import org.json4s.jackson.JsonMethods._
import org.scalatest.{FunSuite, Matchers}
import org.slf4j.MarkerFactory

import scala.collection.JavaConverters._
import scala.util.Random

class GelfLayoutTests extends FunSuite with Matchers {

  val context = new LoggerContext
  val logger = context.getLogger("test")

  test("Uses the correct version") {
    val json = layoutJson(new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null))
    assert((json \ "version").values == "1.1")
  }

  test("Sets a sensible host name") {
    val json = layoutJson(new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null))
    assert((json \ "host").values == InetAddress.getLocalHost.getHostName)
  }

  test("Sets the correct timestamp") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    val json = layoutJson(event)
    assert((json \ "timestamp").values == BigDecimal(event.getTimeStamp) / 1000)
  }

  test("Renders the timestamp without exponential notation") {
    val text = layout(new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null))
    text should include regex """"timestamp":[0-9]+\.[0-9]+,"""
  }

  test("Renders the short and full message including substitutions") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message {}", null, Array[Object]("hello"))
    val json = layoutJson(event)
    assert((json \ "short_message").values == event.getFormattedMessage)
    assert((json \ "full_message").values == event.getFormattedMessage)
  }

  test("Truncates the short message to 128 characters") {
    val longMessage = new Random().nextString(250)
    val event = new LoggingEvent("TestClass", logger, Level.INFO, longMessage, null, null)
    val json = layoutJson(event)
    assert((json \ "short_message").values == longMessage.substring(0, 128))
  }

  test("Includes the event marker") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    event.setMarker(MarkerFactory.getMarker("my marker"))
    val json = layoutJson(event)
    assert((json \ "_marker").values == "my marker")
  }

  test("Includes the event marker with references") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    val marker = MarkerFactory.getMarker("my marker")
    marker.add(MarkerFactory.getMarker("reference 1"))
    marker.add(MarkerFactory.getMarker("reference 2"))
    event.setMarker(marker)
    val json = layoutJson(event)
    assert((json \ "_marker").values == "my marker [ reference 1, reference 2 ]")
  }

  test("Includes MDC properties as additional fields") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    event.setMDCPropertyMap(Map("foo" -> "bar", "hello" -> "world").asJava)
    val json = layoutJson(event)
    assert((json \ "_foo").values == "bar")
    assert((json \ "_hello").values == "world")
  }

  test("Renders numeric MDC properties as numbers") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    event.setMDCPropertyMap(Map("foo" -> "123", "hello" -> "4.56").asJava)
    val json = layoutJson(event)
    assert((json \ "_foo") == JInt(123))
    assert((json \ "_hello") == JDecimal(4.56))
  }

  test("Allows the timestamp to be overridden using the 'timestamp' MDC property") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    event.setMDCPropertyMap(Map("timestamp" -> "1406300201").asJava)
    val json = layoutJson(event)
    assert((json \ "timestamp").values == BigDecimal(1406300201) / 1000)
  }

  test("Uses the event's timestamp if the 'timestamp' MDC property isn't a UNIX timestamp") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    event.setMDCPropertyMap(Map("timestamp" -> "wibble").asJava)
    val json = layoutJson(event)
    assert((json \ "timestamp").values == BigDecimal(event.getTimeStamp) / 1000)
  }

  test("Does not render the 'timestamp' MDC property in the additional fields if it is a UNIX timestamp") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    event.setMDCPropertyMap(Map("timestamp" -> "1406300201").asJava)
    val json = layoutJson(event)
    assert((json \ "_timestamp").values == None)
  }

  test("Renders the 'timestamp' MDC property in the additional fields if it isn't a UNIX timestamp") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    event.setMDCPropertyMap(Map("timestamp" -> "wibble").asJava)
    val json = layoutJson(event)
    assert((json \ "_timestamp").values == "wibble")
  }

  private def layout(event: LoggingEvent) = {
    val layout = new GelfLayout
    layout.start()
    val gelf = layout.doLayout(event)
    layout.stop()
    gelf
  }

  private def layoutJson(event: LoggingEvent) = parse(layout(event), useBigDecimalForDouble = true)
}
