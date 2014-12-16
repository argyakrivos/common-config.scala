package com.blinkbox.books.metrics

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(delegate: ThreadFactory, namePrefix: String) extends ThreadFactory {
  val counter = new AtomicInteger(0)
  override def newThread(r: Runnable): Thread = {
    val thread = delegate.newThread(r)
    thread.setName(s"$namePrefix-thread-${counter.getAndIncrement}")
    thread
  }
}
