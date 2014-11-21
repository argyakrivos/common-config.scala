package com.blinkbox.books

import com.typesafe.scalalogging.Logger
import org.slf4j.MDC

package object logging {

  implicit class RichLogger(logger: Logger) {

    /**
     * Creates a block with a temporary MDC context, which will be reverted when the block completes.
     * @param context The MDC context for the block. This is added to any existing MDC context.
     * @param log A function to log messages using the temporary context.
     */
    def withContext(context: Map[String, Any])(log: Logger => Unit): Unit = withContext(context.toSeq: _*)(log)

    /**
     * Creates a block with a temporary MDC context, which will be reverted when the block completes.
     * @param context The MDC context for the block. This is added to any existing MDC context.
     * @param log A function to log messages using the temporary context.
     */
    def withContext(context: (String, Any)*)(log: Logger => Unit): Unit = {
      val originalMDC = MDC.getCopyOfContextMap
      context.foreach {
        case (k, v) if v != null => MDC.put(k, v.toString)
        case _ =>
      }
      log(logger)
      if (originalMDC == null) MDC.clear() else MDC.setContextMap(originalMDC)
    }

  }

}
