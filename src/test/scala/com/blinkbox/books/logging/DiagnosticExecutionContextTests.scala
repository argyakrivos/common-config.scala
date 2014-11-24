package com.blinkbox.books.logging

import java.util.concurrent.{Executor, Executors, LinkedBlockingQueue}
import java.util.concurrent.atomic.AtomicReference
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}
import org.scalatest.concurrent.AsyncAssertions
import org.slf4j.MDC
import scala.concurrent.ExecutionContext

// a really basic executor that executes a single item and which doesn't propagate any context
// at the point of executing the runnable, which some of the built-in executors appear to do
private class TestExecutor extends Executor {
  val q = new LinkedBlockingQueue[Runnable]()
  val t = new Thread(new Runnable { override def run(): Unit = q.take().run() })
  t.start()
  override def execute(command: Runnable): Unit = q.put(command)
}

class DiagnosticExecutionContextTests extends FunSuite with BeforeAndAfterEach with Matchers with AsyncAssertions {

  override def beforeEach(): Unit = {
    MDC.clear()
  }

  test("Copies execution context from the originating thread to the new thread") {
    val w = new Waiter
    val ec = DiagnosticExecutionContext(ExecutionContext.fromExecutor(new TestExecutor))

    MDC.put("testKey", "testValue")
    val mdcRef = new AtomicReference[java.util.Map[_, _]]()
    ec.prepare().execute(new Runnable {
      override def run(): Unit = {
        mdcRef.set(MDC.getCopyOfContextMap)
        w.dismiss()
      }
    })
    w.await()

    val mdc = mdcRef.get()
    assert(mdc != null && mdc.size() == 1)
    assert(mdc.get("testKey") == "testValue")
  }

  test("Does not propagate MDC changes back to the original thread") {
    val w = new Waiter
    val ec = DiagnosticExecutionContext(ExecutionContext.fromExecutor(new TestExecutor))

    MDC.put("testKey1", "testValue1")
    ec.prepare().execute(new Runnable {
      override def run(): Unit = {
        MDC.put("testKey2", "testValue2")
        w.dismiss()
      }
    })
    w.await()

    assert(MDC.get("testKey1") == "testValue1")
    assert(MDC.get("testKey2") == null)
  }

  test("Requires the use of prepare to create a valid context for execute") {
    val e = intercept[UnsupportedOperationException] {
      val ec = DiagnosticExecutionContext(ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor))
      ec.execute(new Runnable { override def run(): Unit = {} })
    }
    assert(e.getMessage.matches(".*prepare.*")) // should tell the user to use the prepare method
  }

  test("Requires the use of prepare to create a valid context for reportFailure") {
    val e = intercept[UnsupportedOperationException] {
      val ec = DiagnosticExecutionContext(ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor))
      ec.reportFailure(new Exception())
    }
    assert(e.getMessage.matches(".*prepare.*")) // should tell the user to use the prepare method
  }

}
