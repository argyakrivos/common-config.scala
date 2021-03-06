# common-config

Contains code to bootstrap your Scala applications.

- [Configuration](#configuration)
- [Logging](#logging)
- [Metrics](#metrics)
 
## Configuration

[HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) configuration is loaded from the system properties, `application.conf` and `reference.conf` in the usual way using the [Typesafe Config library](https://github.com/typesafehub/config). However, if there is an environment variable `CONFIG_URL` defined then config will also be loaded from this and override any local settings _except_ those specified in the system properties. In other words, the order of precedence for config settings is:

 - System properties (e.g. `java -Dmyapp.foo.bar=10`)
 - The configuration at `CONFIG_URL`
 - `application.conf`
 - `reference.conf`

The following URL schemes are supported for `CONFIG_URL`:

 - `http` and `https`, which are ideal for production systems with a centralised configuration service.
 - `file`, which is ideal for  production systems using a configuration management system such as puppet to create application-specific configuration files.
 - `classpath`, which is intended for development or testing purposes so that you don't need any external dependencies. It's highly recommended that you use file names that won't be mistaken for production configuration such as `testing.conf`.

### Configuration structure

When using this library, the recommended way to structure your configuration is as follows:

 - Put all the settings that have sensible defaults (e.g. log level, timeouts) into `reference.conf` as a baseline configuration. Do not put any environment-specific settings such as URLs or port numbers in here!
 - Put all the settings that have sensible defaults on a dev workstation into `application.conf` -- this might include standard local URLs, port numbers, database names, etc. to make it easy for people to work on the project.
 - Use the configuration pointed to by `CONFIG_URL` for the majority of the runtime configuration (i.e. the environment-specific settings) in environments other than development (e.g. CI build, QA, production).
 - Use the `-D` overrides for any local overrides. These might be temporary overrides such as log level, or to allow sensitive settings such as private keys to be specified separately from shared configuration.

There are a number of standard configuration setting names, and a recommended structure for application setting names described [in Confluence](http://jira.blinkbox.local/confluence/display/PT/Service+Configuration+Guidelines).

### Excluding dev config from JARs

Because the dev config settings are in `application.conf` it's _really_ important that this file is excluded from any JARs that are built or they might accidentally get loaded as the production settings! In your `build.sbt` file make sure you add something like this which will discard the file when building the JAR:

~~~scala
mergeStrategy in assembly <<= (mergeStrategy in assembly) { old =>
  {
    case "application.conf" => MergeStrategy.discard
    case x => old(x)
  }
}
~~~

### Configuring your app

It really couldn't be simpler. Just mix in the `Configuration` trait and you'll have a new `config` field available to you with the loaded and merged configuration, e.g.

~~~scala
import com.blinkbox.books.config.Configuration

object MyApp extends App with Configuration {
  println(config) // or do something more useful
}
~~~

To validate the configuration, as well as transforming it into a more usable format, it may be useful to build a strongly-typed eagerly-evaluated configuration wrapper around this loosely typed property bag. An example of this is below, which validates that all the required properties are present and transforms some of them into more useful objects such as gateways or URLs:

~~~scala
case class AppConfig(braintree: BraintreeConfig, service: ServiceConfig)

case class BraintreeConfig(environment: Environment, merchantId: String, publicKey: String, privateKey: String) {
  val gateway = new BraintreeGateway(environment, merchantId, publicKey, privateKey)
}

case class ServiceConfig(auth: URL)

object AppConfig {
  def apply(config: Config): AppConfig = AppConfig(BraintreeConfig(config), ServiceConfig(config))
}

object BraintreeConfig {
  def apply(config: Config): BraintreeConfig = {
    val environment = config.getString("braintree.environment") match {
      case "DEVELOPMENT" => Environment.DEVELOPMENT
      case "SANDBOX" => Environment.SANDBOX
      case "PRODUCTION" => Environment.PRODUCTION
      case env => throw new BadValue(config.origin, "braintree.environment", s"Unknown: '$env'.")
    }
    val merchantId = config.getString("braintree.merchantId")
    val publicKey = config.getString("braintree.publicKey")
    val privateKey = config.getString("braintree.privateKey")
    BraintreeConfig(environment, merchantId, publicKey, privateKey)
  }
}

object ServiceConfig {
  def apply(config: Config): ServiceConfig = ServiceConfig(config.getHttpUrl("service.auth.uri"))
}

object MyApp extends App with Configuration {
  val appConfig = AppConfig(config)
}
~~~

## Logging

All logging should be done in [GELF](http://graylog2.org/gelf) format, and in rig environments it is sent via UDP to the log server. To support this the library contains some [Logback](http://logback.qos.ch/) extensions which can be automatically configured by mixing the `Loggers` trait into your app, e.g.

~~~scala
import com.blinkbox.books.config._
import com.blinkbox.books.logging._
import com.typesafe.scalalogging.slf4j.StrictLogging
import org.slf4j.MDC

object MyApp extends App with Configuration with Loggers with StrictLogging {
  MDC.put("foo", "bar")      // add a filterable property to log messages
  logger.info("App started") // this will be logged as GELF over UDP
}
~~~

If the MDC context only applies to the particular logging call, and not to subsequent calls, then you can scope the MDC as follows:

~~~scala
object MyApp extends App with Configuration with Loggers with Logging {
  logger.withContext("foo" -> "bar")(_.info("App started"))
}
~~~

If you log an MDC property named `timestamp` containing a UNIX timestamp then it will override the timestamp that the event is reported with. This can be useful for processes where you want to record the event at the start time rather than the finish time. You should only use this for short-running processes, maximum around 10 seconds, to ensure that it doesn't cause problems in the aggregating servers with significantly out-of-order events.

To test the logging out on your machine, the easiest option is to vagrant up the [graylog-vagrant virtual machine](https://git.mobcastdev.com/ITOPS/graylog-vagrant).

Any information placed in the [MDC](http://logback.qos.ch/manual/mdc.html) will be sent as properties in the log messages, which are very useful for filtering them. When using futures, ensure you wrap any execution contexts with `DiagnosticExecutionContext` to ensure that MDC state is propagated correctly.

## Metrics

The library integrates with Dropwizard Metrics (formerly Codahale Metrics) to improve the ability to monitor software. These can be logged to Graylog and/or New Relic, as well as more mundane places like the console.

Add instrumentation to your thread pools by wrapping them in an `InstrumentedThreadPoolExecutor`. This will give you metrics useful for tuning them, including:

* Pool size (gauge)
* Queue size (gauge)
* Submitted count (meter)
* Completed count (meter)
* Rejected count (meter)
* Running count (counter)
* Task duration (timer)

Using the constructor for `DiagnosticExecutionContext` that takes a `MetricRegistry` will apply these to the created context for you.
