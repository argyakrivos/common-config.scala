package com.blinkbox.books.metrics

import java.util
import java.util.concurrent.TimeUnit

import com.blinkbox.books.logging.RichLogger
import com.codahale.metrics._
import com.typesafe.scalalogging.Logger
import org.slf4j.{LoggerFactory, Marker}

import scala.collection.JavaConversions._
import scala.collection.immutable.ListMap

object Slf4jReporter {
  def forRegistry(registry: MetricRegistry): Builder = new Builder(registry)

  class Builder(registry: MetricRegistry) {
    private[this] var logger: Logger = Logger(LoggerFactory.getLogger("com.blinkbox.books.metrics.slf4j"))
    private[this] var rateUnit: TimeUnit = TimeUnit.SECONDS
    private[this] var durationUnit: TimeUnit = TimeUnit.MILLISECONDS
    private[this] var filter: MetricFilter = MetricFilter.ALL
    private[this] var marker: Marker = null

    def outputTo(logger: Logger): Builder = { this.logger = logger; this }
    def markWith(marker: Marker): Builder = { this.marker = marker; this }
    def convertRatesTo(rateUnit: TimeUnit): Builder = { this.rateUnit = rateUnit; this }
    def convertDurationsTo(durationUnit: TimeUnit): Builder = { this.durationUnit = durationUnit; this }
    def filter(filter: MetricFilter): Builder = { this.filter = filter; this }

    def build(): Slf4jReporter = new Slf4jReporter(registry, logger, marker, rateUnit, durationUnit, filter)
  }
}

class Slf4jReporter(registry: MetricRegistry, logger: Logger, marker: Marker, rateUnit: TimeUnit, durationUnit: TimeUnit, filter: MetricFilter)
  extends ScheduledReporter(registry, "slf4j-reporter", filter, rateUnit, durationUnit) {

  def report(gauges: util.SortedMap[String, Gauge[_]], counters: util.SortedMap[String, Counter], histograms: util.SortedMap[String, Histogram], meters: util.SortedMap[String, Meter], timers: util.SortedMap[String, Timer]): Unit = {
    for (entry <- gauges.entrySet) logGauge(entry.getKey, entry.getValue)
    for (entry <- counters.entrySet) logCounter(entry.getKey, entry.getValue)
    for (entry <- histograms.entrySet) logHistogram(entry.getKey, entry.getValue)
    for (entry <- meters.entrySet) logMeter(entry.getKey, entry.getValue)
    for (entry <- timers.entrySet) logTimer(entry.getKey, entry.getValue)
  }

  private def logTimer(name: String, timer: Timer): Unit = {
    val snapshot = timer.getSnapshot
    val context = ListMap(
      "metricType" -> "timer",
      "metricName" -> name,
      "count" -> timer.getCount,
      "min" -> convertDuration(snapshot.getMin),
      "max" -> convertDuration(snapshot.getMax),
      "mean" -> convertDuration(snapshot.getMean),
      "stddev" -> convertDuration(snapshot.getStdDev),
      "median" -> convertDuration(snapshot.getMedian),
      "75thPercentile" -> convertDuration(snapshot.get75thPercentile),
      "95thPercentile" -> convertDuration(snapshot.get95thPercentile),
      "98thPercentile" -> convertDuration(snapshot.get98thPercentile),
      "99thPercentile" -> convertDuration(snapshot.get99thPercentile),
      "999thPercentile" -> convertDuration(snapshot.get999thPercentile),
      "meanRate" -> convertRate(timer.getMeanRate),
      "1minRate" -> convertRate(timer.getOneMinuteRate),
      "5minRate" -> convertRate(timer.getFiveMinuteRate),
      "15minRate" -> convertRate(timer.getFifteenMinuteRate),
      "rateUnit" -> getRateUnit,
      "durationUnit" -> getDurationUnit)
    logger.withContext(context)(_.info(marker, message(context)))
  }

  private def logMeter(name: String, meter: Meter): Unit = {
    val context = ListMap(
      "metricType" -> "meter",
      "metricName" -> name,
      "count" -> meter.getCount,
      "meanRate" -> convertRate(meter.getMeanRate),
      "1minRate" -> convertRate(meter.getOneMinuteRate),
      "5minRate" -> convertRate(meter.getFiveMinuteRate),
      "15minRate" -> convertRate(meter.getFifteenMinuteRate),
      "rateUnit" -> getRateUnit)
    logger.withContext(context)(_.info(marker, message(context)))
  }

  private def logHistogram(name: String, histogram: Histogram): Unit = {
    val snapshot = histogram.getSnapshot
    val context = ListMap(
      "metricType" -> "histogram",
      "metricName" -> name,
      "count" -> histogram.getCount,
      "min" -> convertDuration(snapshot.getMin),
      "max" -> convertDuration(snapshot.getMax),
      "mean" -> convertDuration(snapshot.getMean),
      "stddev" -> convertDuration(snapshot.getStdDev),
      "median" -> convertDuration(snapshot.getMedian),
      "75thPercentile" -> convertDuration(snapshot.get75thPercentile),
      "95thPercentile" -> convertDuration(snapshot.get95thPercentile),
      "98thPercentile" -> convertDuration(snapshot.get98thPercentile),
      "99thPercentile" -> convertDuration(snapshot.get99thPercentile),
      "999thPercentile" -> convertDuration(snapshot.get999thPercentile))
    logger.withContext(context)(_.info(marker, message(context)))
  }

  private def logCounter(name: String, counter: Counter): Unit = {
    val context = ListMap("metricType" -> "counter", "metricName" -> name, "count" -> counter.getCount)
    logger.withContext(context)(_.info(marker, message(context)))
  }

  private def logGauge(name: String, gauge: Gauge[_]): Unit = {
    val context = ListMap("metricType" -> "gauge", "metricName" -> name, "value" -> gauge.getValue)
    logger.withContext(context)(_.info(marker, message(context)))
  }

  protected override def getRateUnit: String = "events/" + super.getRateUnit

  private def message(context: Map[String, Any]): String = context map {
    case (k, v) => s"$k=$v"
  } mkString ", "
}
