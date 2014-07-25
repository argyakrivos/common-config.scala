package com.blinkbox.books.logging

import java.util.concurrent.atomic.AtomicReference

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger => ClassicLogger}
import ch.qos.logback.core.Appender
import com.typesafe.scalalogging.slf4j.Logging
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.slf4j.{Logger => Slf4jLogger, LoggerFactory => Slf4jLoggerFactory}

class WithContextTests extends FunSuite with MockitoSugar with Logging {

  test("logger.withContext sets the context only within the passed block") {
    val mdcRef1 = new AtomicReference[Option[java.util.Map[String, String]]](None)
    val mdcRef2 = new AtomicReference[Option[java.util.Map[String, String]]](None)

    val appender = mock[Appender[ILoggingEvent]]
    when(appender.getName).thenReturn("MOCK")
    when(appender.doAppend(any[ILoggingEvent])).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit =
        mdcRef1.set(Some(invocation.getArguments()(0).asInstanceOf[ILoggingEvent].getMDCPropertyMap))
    }).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit =
        mdcRef2.set(Some(invocation.getArguments()(0).asInstanceOf[ILoggingEvent].getMDCPropertyMap))
    })

    val rootLogger = Slf4jLoggerFactory.getLogger(Slf4jLogger.ROOT_LOGGER_NAME).asInstanceOf[ClassicLogger]
    rootLogger.detachAndStopAllAppenders()
    rootLogger.addAppender(appender)

    logger.withContext("k1" -> "v1")(_.debug("hello world"))
    logger.debug("hello world")

    assert(mdcRef1.get.get.get("k1") == "v1")
    assert(!mdcRef2.get.get.containsKey("k1"))
  }

  test("keys with null values are not added to the MDC") {
    val mdcRef = new AtomicReference[Option[java.util.Map[String, String]]](None)

    val appender = mock[Appender[ILoggingEvent]]
    when(appender.getName).thenReturn("MOCK")
    when(appender.doAppend(any[ILoggingEvent])).thenAnswer(new Answer[Unit] {
      override def answer(invocation: InvocationOnMock): Unit =
        mdcRef.set(Some(invocation.getArguments()(0).asInstanceOf[ILoggingEvent].getMDCPropertyMap))
    })

    val rootLogger = Slf4jLoggerFactory.getLogger(Slf4jLogger.ROOT_LOGGER_NAME).asInstanceOf[ClassicLogger]
    rootLogger.detachAndStopAllAppenders()
    rootLogger.addAppender(appender)

    logger.withContext("k1" -> 123, "k2" -> null)(_.debug("hello world"))

    assert(mdcRef.get.get.get("k1") == "123")
    assert(!mdcRef.get.get.containsKey("k2"))
  }

}
