package com.blinkbox.books.logging

import org.slf4j.MDC
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
 * Contains factory methods for creating diagnostic execution contexts.
 */
object DiagnosticExecutionContext {
  def apply(delegate: ExecutionContext): ExecutionContextExecutor = new DiagnosticExecutionContext(delegate)
}

/**
 * A wrapper around execution contexts that copies the Mapped Diagnostic Context (MDC) between threads
 * and sets/resets it around execution of runnables.
 * @param delegate The execution context to delegate running to.
 */
class DiagnosticExecutionContext(delegate: ExecutionContext) extends ExecutionContextExecutor {
  override def prepare(): ExecutionContext = new ChainedDiagnosticExecutionContext(delegate)
  override def execute(runnable: Runnable) = delegate.execute(runnable)
  override def reportFailure(t: Throwable) = delegate.reportFailure(t)
}

/**
 * Execution context wrapper that captures the MDC state of a thread at the point of creation.
 * @param delegate The execution context to delegate running to.
 */
private class ChainedDiagnosticExecutionContext(delegate: ExecutionContext) extends ExecutionContextExecutor {
  private val capturedMDC = MDC.getCopyOfContextMap

  override def execute(runnable: Runnable) = delegate.execute(new Runnable {
    def run() {
      val originalMDC = MDC.getCopyOfContextMap
      setMDC(capturedMDC)
      try runnable.run()
      finally setMDC(originalMDC)
    }
  })

  override def reportFailure(t: Throwable) = delegate.reportFailure(t)

  private def setMDC(mdc: java.util.Map[_, _]): Unit = if (mdc == null) MDC.clear() else MDC.setContextMap(mdc)
}