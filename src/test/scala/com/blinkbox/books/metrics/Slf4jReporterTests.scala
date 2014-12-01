package com.blinkbox.books.metrics

import java.util
import java.util.concurrent.TimeUnit

import com.blinkbox.books.test.MockitoSyrup
import com.codahale.metrics._
import com.typesafe.scalalogging.Logger
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.slf4j.{MDC, Marker, Logger => Slf4jLogger}

class Slf4jReporterTests extends FunSuite with MockitoSyrup {

  test("Reports timer metrics in the MDC") {
    val metrics = new MetricRegistry
    val timers = new java.util.TreeMap[String, Timer]
    timers.put("test-timer", metrics.timer("test-timer"))

    val mdc = getLoggedMdc(metrics, timers = timers)
    assert(mdc.get("metricType") == "timer")
    assert(mdc.get("metricName") == "test-timer")
    assert(mdc.get("count") == "0")
    assert(mdc.get("min") == "0.0")
    assert(mdc.get("max") == "0.0")
    assert(mdc.get("mean") == "0.0")
    assert(mdc.get("stddev") == "0.0")
    assert(mdc.get("median") == "0.0")
    assert(mdc.get("75thPercentile") == "0.0")
    assert(mdc.get("95thPercentile") == "0.0")
    assert(mdc.get("98thPercentile") == "0.0")
    assert(mdc.get("99thPercentile") == "0.0")
    assert(mdc.get("999thPercentile") == "0.0")
    assert(mdc.get("meanRate") == "0.0")
    assert(mdc.get("1minRate") == "0.0")
    assert(mdc.get("5minRate") == "0.0")
    assert(mdc.get("15minRate") == "0.0")
    assert(mdc.get("rateUnit") == "events/second")
    assert(mdc.get("durationUnit") == "milliseconds")
  }

  test("Reports meter metrics in the MDC") {
    val metrics = new MetricRegistry
    val meters = new java.util.TreeMap[String, Meter]
    meters.put("test-meter", metrics.meter("test-meter"))

    val mdc = getLoggedMdc(metrics, meters = meters)
    assert(mdc.get("metricType") == "meter")
    assert(mdc.get("metricName") == "test-meter")
    assert(mdc.get("count") == "0")
    assert(mdc.get("meanRate") == "0.0")
    assert(mdc.get("1minRate") == "0.0")
    assert(mdc.get("5minRate") == "0.0")
    assert(mdc.get("15minRate") == "0.0")
    assert(mdc.get("rateUnit") == "events/second")
  }

  test("Reports histogram metrics in the MDC") {
    val metrics = new MetricRegistry
    val histograms = new java.util.TreeMap[String, Histogram]
    histograms.put("test-histogram", metrics.histogram("test-histogram"))

    val mdc = getLoggedMdc(metrics, histograms = histograms)
    assert(mdc.get("metricType") == "histogram")
    assert(mdc.get("metricName") == "test-histogram")
    assert(mdc.get("count") == "0")
    assert(mdc.get("min") == "0.0")
    assert(mdc.get("max") == "0.0")
    assert(mdc.get("mean") == "0.0")
    assert(mdc.get("stddev") == "0.0")
    assert(mdc.get("median") == "0.0")
    assert(mdc.get("75thPercentile") == "0.0")
    assert(mdc.get("95thPercentile") == "0.0")
    assert(mdc.get("98thPercentile") == "0.0")
    assert(mdc.get("99thPercentile") == "0.0")
    assert(mdc.get("999thPercentile") == "0.0")
  }

  test("Reports counter metrics in the MDC") {
    val metrics = new MetricRegistry
    val counters = new java.util.TreeMap[String, Counter]
    counters.put("test-counter", metrics.counter("test-counter"))

    val mdc = getLoggedMdc(metrics, counters = counters)
    assert(mdc.get("metricType") == "counter")
    assert(mdc.get("metricName") == "test-counter")
    assert(mdc.get("count") == "0")
  }

  test("Reports gauge metrics in the MDC") {
    val metrics = new MetricRegistry
    val gauges = new java.util.TreeMap[String, Gauge[_]]
    gauges.put("test-gauge", metrics.register("test-gauge", new Gauge[Int] { override def getValue: Int = 123 }))

    val mdc = getLoggedMdc(metrics, gauges = gauges)
    assert(mdc.get("metricType") == "gauge")
    assert(mdc.get("metricName") == "test-gauge")
    assert(mdc.get("value") == "123")
  }

  private def getLoggedMdc(
    metrics: MetricRegistry,
    gauges: util.SortedMap[String, Gauge[_]] = new util.TreeMap[String, Gauge[_]],
    counters: util.SortedMap[String, Counter] = new util.TreeMap[String, Counter],
    histograms: util.SortedMap[String, Histogram] = new util.TreeMap[String, Histogram],
    meters: util.SortedMap[String, Meter] = new util.TreeMap[String, Meter],
    timers: util.SortedMap[String, Timer] = new util.TreeMap[String, Timer]): util.Map[String, String] = {

    var mdc = Option.empty[util.Map[String, String]]
    val logger = mock[Slf4jLogger]
    when(logger.isInfoEnabled).thenAnswer(() => true)
    when(logger.info(any[Marker], any[String])).thenAnswer(() => { mdc = Option(MDC.getCopyOfContextMap) })

    val reporter = Slf4jReporter.forRegistry(metrics).outputTo(Logger(logger)).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build()
    reporter.report(gauges, counters, histograms, meters, timers)

    assert(mdc.isDefined)
    mdc.get
  }

}
