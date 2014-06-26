package com.blinkbox.books.logging

import ch.qos.logback.classic.{Logger => ClassicLogger, LoggerContext}
import ch.qos.logback.classic.spi.ILoggingEvent
import com.blinkbox.books.config._
import com.blinkbox.books.logging.gelf.{GelfLayout, UdpAppender}
import org.slf4j.{Logger, LoggerFactory}

/**
 * A trait that can be mixed in to add the standard blinkbox books logging configuration.
 */
trait Logging {
  this: Configuration =>

  val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

  val layout = new GelfLayout
  layout.facility = config.getString("graylog.gelf.facility")
  config.getStringOption("graylog.gelf.shortMessagePattern").foreach(layout.shortMessagePattern = _)
  config.getStringOption("graylog.gelf.fullMessagePattern").foreach(layout.fullMessagePattern = _)
  config.getBooleanOption("graylog.gelf.includeLoggerName").foreach(layout.includeLoggerName = _)
  config.getBooleanOption("graylog.gelf.includeThreadName").foreach(layout.includeThreadName = _)
  layout.setContext(context)
  layout.start()

  val appender = new UdpAppender[ILoggingEvent]
  appender.host = config.getString("graylog.server.host")
  config.getIntOption("graylog.server.port").foreach(appender.port = _)
  config.getIntOption("graylog.gelf.maxChunkSize").foreach(appender.maxChunkSize = _)

  appender.setLayout(layout)
  appender.setContext(context)
  appender.start()

  val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ClassicLogger]
  logger.detachAndStopAllAppenders()
  logger.addAppender(appender)
}
