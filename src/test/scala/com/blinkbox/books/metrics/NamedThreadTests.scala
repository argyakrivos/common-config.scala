package com.blinkbox.books.metrics

import java.util.concurrent.Executors

import org.scalatest.FunSuite

class NamedThreadTests extends FunSuite {

  test("Creates threads with a name prefix and sequential identifier") {
    val factory = new NamedThreadFactory(Executors.defaultThreadFactory, "test-pool")
    val thread0 = factory.newThread(new Runnable { override def run(): Unit = {} })
    val thread1 = factory.newThread(new Runnable { override def run(): Unit = {} })
    assert(thread0.getName == "test-pool-thread-0")
    assert(thread1.getName == "test-pool-thread-1")
  }

}
