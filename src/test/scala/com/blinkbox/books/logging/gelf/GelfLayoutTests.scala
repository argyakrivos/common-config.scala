package com.blinkbox.books.logging.gelf

import ch.qos.logback.classic.{Level, LoggerContext}
import ch.qos.logback.classic.spi.{ThrowableProxyVO, LoggingEvent}
import java.net.InetAddress
import org.json4s.jackson.JsonMethods._
import org.scalatest.{Matchers, FunSuite}
import org.slf4j.MarkerFactory
import scala.collection.JavaConversions._
import scala.util.Random

class GelfLayoutTests extends FunSuite with Matchers {

  val context = new LoggerContext
  val logger = context.getLogger("test")

  test("Uses the correct version") {
    val json = layoutJson(new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null))
    assert((json \ "version").values === "1.1")
  }

  test("Sets a sensible host name") {
    val json = layoutJson(new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null))
    assert((json \ "host").values === InetAddress.getLocalHost.getHostName)
  }

  test("Sets the correct timestamp") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    val json = layoutJson(event)
    assert((json \ "timestamp").values === BigDecimal(event.getTimeStamp) / 1000)
  }

  test("Renders the timestamp without exponential notation") {
    val text = layout(new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null))
    text should include regex """"timestamp":[0-9]+\.[0-9]+,"""
  }

  test("Renders the short and full message including substitutions") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message {}", null, Array[Object]("hello"))
    val json = layoutJson(event)
    assert((json \ "short_message").values === event.getFormattedMessage)
    assert((json \ "full_message").values === event.getFormattedMessage)
  }

  test("Truncates the short message to 128 characters") {
    val longMessage = new Random().nextString(250)
    val event = new LoggingEvent("TestClass", logger, Level.INFO, longMessage, null, null)
    val json = layoutJson(event)
    assert((json \ "short_message").values === longMessage.substring(0, 128))
  }

  test("Includes the event marker") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    event.setMarker(MarkerFactory.getMarker("my marker"))
    val json = layoutJson(event)
    assert((json \ "_marker").values === "my marker")
  }

  test("Includes the event marker with references") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    val marker = MarkerFactory.getMarker("my marker")
    marker.add(MarkerFactory.getMarker("reference 1"))
    marker.add(MarkerFactory.getMarker("reference 2"))
    event.setMarker(marker)
    val json = layoutJson(event)
    assert((json \ "_marker").values === "my marker [ reference 1, reference 2 ]")
  }

  test("Includes MDC properties as additional fields") {
    val event = new LoggingEvent("TestClass", logger, Level.INFO, "test message", null, null)
    event.setMDCPropertyMap(mapAsJavaMap(Map("foo" -> "bar", "hello" -> "world")))
    val json = layoutJson(event)
    assert((json \ "_foo").values === "bar")
    assert((json \ "_hello").values === "world")
  }

  private def layout(event: LoggingEvent) = {
    ThrowableProxyVO
    val layout = new GelfLayout
    layout.start()
    val gelf = layout.doLayout(event)
    layout.stop()
    gelf
  }

  private def layoutJson(event: LoggingEvent) = parse(layout(event), useBigDecimalForDouble = true)
}
