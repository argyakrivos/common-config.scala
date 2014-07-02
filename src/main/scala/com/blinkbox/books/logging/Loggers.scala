package com.blinkbox.books.logging

import ch.qos.logback.classic.{Level, Logger => ClassicLogger, LoggerContext}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import com.blinkbox.books.config._
import com.blinkbox.books.logging.gelf.{GelfLayout, UdpAppender}
import org.slf4j.{Logger, LoggerFactory}

/**
 * A trait that can be mixed into apps to configure the standard loggers.
 */
trait Loggers {
  this: App with Configuration =>

  private val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

  private val gelfLayout = new GelfLayout
  gelfLayout.facility = config.getString("logging.gelf.facility")
  config.getStringOption("logging.gelf.shortMessagePattern").foreach(gelfLayout.shortMessagePattern = _)
  config.getStringOption("logging.gelf.fullMessagePattern").foreach(gelfLayout.fullMessagePattern = _)
  config.getBooleanOption("logging.gelf.includeLoggerName").foreach(gelfLayout.includeLoggerName = _)
  config.getBooleanOption("logging.gelf.includeThreadName").foreach(gelfLayout.includeThreadName = _)
  gelfLayout.setContext(loggerContext)
  gelfLayout.start()

  private val udpAppender = new UdpAppender[ILoggingEvent]
  udpAppender.setName("UDP")
  udpAppender.host = config.getString("logging.udp.host")
  config.getIntOption("logging.udp.port").foreach(udpAppender.port = _)
  config.getIntOption("logging.udp.maxChunkSize").foreach(udpAppender.maxChunkSize = _)
  udpAppender.setLayout(gelfLayout)
  udpAppender.setContext(loggerContext)
  udpAppender.start()

  private lazy val consoleAppender = {
    val encoder = new LayoutWrappingEncoder[ILoggingEvent]
    encoder.init(System.out)
    encoder.setImmediateFlush(true)
    encoder.setLayout(gelfLayout)
    encoder.setContext(loggerContext)
    encoder.start()

    val appender = new ConsoleAppender[ILoggingEvent]
    appender.setName("CONSOLE")
    appender.setEncoder(encoder)
    appender.setContext(loggerContext)
    appender.start()
    appender
  }

  private val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ClassicLogger]
  rootLogger.setLevel(Level.toLevel(config.getStringOption("logging.level").getOrElse(null)))
  rootLogger.detachAndStopAllAppenders()
  rootLogger.addAppender(udpAppender)
  config.getBooleanOption("logging.console.enabled").foreach(if (_) rootLogger.addAppender(consoleAppender))
}
