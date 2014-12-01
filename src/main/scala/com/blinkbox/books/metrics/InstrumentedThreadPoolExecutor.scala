package com.blinkbox.books.metrics

import java.util.concurrent.ThreadPoolExecutor

import com.codahale.metrics.{Gauge, InstrumentedExecutorService, MetricRegistry}

class InstrumentedThreadPoolExecutor(delegate: ThreadPoolExecutor, registry: MetricRegistry, name: String)
  extends InstrumentedExecutorService(delegate, registry, name) {
  registry.register(MetricRegistry.name(name, "size"), new Gauge[Int] {
    override def getValue = delegate.getPoolSize
  })
  registry.register(MetricRegistry.name(name, "remainingCapacity"), new Gauge[Int] {
    override def getValue = delegate.getMaximumPoolSize - delegate.getPoolSize
  })
  registry.register(MetricRegistry.name(name, "queue", "size"), new Gauge[Int] {
    override def getValue = delegate.getQueue.size
  })
  registry.register(MetricRegistry.name(name, "queue", "remainingCapacity"), new Gauge[Int] {
    override def getValue = delegate.getQueue.remainingCapacity
  })
}