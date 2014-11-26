# Change Log

## 2.1.0 ([#32](https://git.mobcastdev.com/Platform/common-config/pull/32) 2014-11-26 17:18:37)

Added configuration for thread pools

### New features

- Now has a `ThreadPoolConfig` class to load configuration for bounded
thread pools.
- `DiagnosticExecutionContext` has a new `apply` method which can
create a context from a `ThreadPoolConfig` instance.

## 2.0.1 ([#31](https://git.mobcastdev.com/Platform/common-config/pull/31) 2014-11-24 13:47:04)

Fixed a bunch of warnings

### Improvements

- Changed sbt file to newer ‘multi module’ style.
- Added some stricter compiler flags and fixed a bunch of warnings.

## 2.0.0 ([#29](https://git.mobcastdev.com/Platform/common-config/pull/29) 2014-11-24 11:01:22)

### Breaking changes

- Changed to scala-logging 3.1.0 as we have seen some very strange
behaviour with the 2.x builds which is fixed in 3.x
- Dropped support for Scala 2.10 as the later logging library requires
2.11.

## 1.4.1 ([#28](https://git.mobcastdev.com/Platform/common-config/pull/28) 2014-09-25 14:05:43)

Removed incorrect config setting

### Bugfix

- Removes `reference.conf` with `akka.loggers` override. We should not put library setting overrides in reference configuration [(CP-1879)](http://jira.blinkbox.local/jira/browse/CP-1879). Furthermore, `akka.loggers.0=akka.event.slf4j.Slf4jLogger` notation clashes with 'list' variant of the same setting (`akka.loggers=[akka.event.slf4j.Slf4jLogger]`) because of [this](https://github.com/typesafehub/config/issues/101) bug in Typesafe config library.

## 1.4.0 ([#26](https://git.mobcastdev.com/Platform/common-config/pull/26) 2014-09-22 10:08:25)

Introduce setting for JVM DNS cache TTL

### New feature

As it turns out that the JVM has a DNS cache and that in most cases this has TTL "forever" this PR introduces a setting that can be used to change the TTL value to something more useful in a cloud environment.

## 1.3.0 ([#27](https://git.mobcastdev.com/Platform/common-config/pull/27) 2014-09-23 13:55:36)

Introduce level configuration for specific loggers

### New feature

It is now possible to configure log levels for a specific logger; as an example the following setting with the `logback.xml` file:

    <logger name="scala.slick" level="INFO" />
    <logger name="scala.slick.jdbc.JdbcBackend.statement" level="DEBUG" />
    <logger name="com.zaxxer.hikari.pool.HikariPool" level="INFO" />

would translate to the following in `application.conf`:

    logging.loggers [
      { name: scala.slick, level: INFO }
      { name: scala.slick.jdbc.JdbcBackend.statement, level: DEBUG}
      { name: com.zaxxer.hikari.pool.HikariPool, level: INFO }
    ]


## 1.2.1 ([#25](https://git.mobcastdev.com/Platform/common-config/pull/25) 2014-09-08 09:55:51)

Now resolves config placeholders.

### Bug fixes

- Now calls `resolve` on the loaded configuration which performs
substitution on `${…}` values. Without this you’d get an exception
about unresolved values if these were used.

## 1.2.0 ([#24](https://git.mobcastdev.com/Platform/common-config/pull/24) 2014-09-04 18:02:39)

Console output in readable format

### New Feature

- `console.pattern` setting to enable log output in more readable format. 
- `console.pattern: simple` outputs log messages in a default format.
- The output pattern can be customised: `console.pattern: "%-5level [%thread]: %message%n"`

## 1.1.0 ([#23](https://git.mobcastdev.com/Platform/common-config/pull/23) 2014-09-03 15:04:58)

Introduce FiniteDuration configuration extractors

### New Feature

- Added a `getFiniteDuration` method to the configuration object.

## 1.0.2 ([#22](https://git.mobcastdev.com/Platform/common-config/pull/22) 2014-08-29 14:44:17)

Cross compile to Scala 2.11

### Improvements

- Now cross-compiles to Scala 2.11.
- Updated dependencies to latest versions.

## 1.0.1 ([#21](https://git.mobcastdev.com/Platform/common-config/pull/21) 2014-08-14 17:43:13)

DatabaseConfig

### Improvements

* Added some extra fields for DatabaseConfig
* Handling db config errors properly

## 1.0.0 ([#20](https://git.mobcastdev.com/Platform/common-config/pull/20) 2014-08-06 15:46:56)

Bumped Major version

### Breaking changes

* Bumped the major version

## 0.10.1 ([#19](https://git.mobcastdev.com/Platform/common-config/pull/19) 2014-08-06 15:31:32)

Updated README

### Improvements

* Updated README

## 0.10.0 ([#17](https://git.mobcastdev.com/Platform/common-config/pull/17) 2014-08-06 14:26:40)

Upgraded to scala-logging-slf4j 2.1.2

### Breaking changes

* It seems that scalalogging-slf4j is no more... they just added a dash and it's now scala-logging-slf4j!
* Bumped to the latest version

## 0.9.0 ([#16](https://git.mobcastdev.com/Platform/common-config/pull/16) 2014-07-25 15:08:51)

Added ability to override event timestamp

### New features

- Can now set a `timestamp` property in MDC to a UNIX timestamp to
override the timestamp of logging events.

## 0.8.0 ([#15](https://git.mobcastdev.com/Platform/common-config/pull/15) 2014-07-25 13:18:15)

Now makes it easier to log with temporary MDC

### New features

- Added a `withContext` method to `Logger` so that it’s easy to log
with MDC that only applies to a particular set of logging statements.
- Now references the `com.typesafe.scalalogging` package so you can use
`with Logging` on classes rather than having to use `LoggerFactory`.

## 0.7.1 ([#14](https://git.mobcastdev.com/Platform/common-config/pull/14) 2014-07-22 08:59:35)

CP-1567: Added getListOption function

improvement
added getListOption function 

## 0.7.0 ([#13](https://git.mobcastdev.com/Platform/common-config/pull/13) 2014-07-01 13:27:06)

Added ability to set log level and log to console

### Breaking changes

- The configuration property names now start with `logging.` rather
than `graylog.`, as the latter didn’t make sense in all contexts (e.g.
level, console).

### New features

- You can now set the logging level with the `logging.level`
configuration property.
- You can now log to the console (stdout) by setting
`logging.console.enabled=true`.

## 0.6.2 ([#12](https://git.mobcastdev.com/Platform/common-config/pull/12) 2014-06-30 10:31:15)

Log numeric MDC values as numbers

### Improvements

- Numeric MDC values are logged as numbers rather than strings which allows Graylog to perform aggregates over them.

## 0.6.1 ([#11](https://git.mobcastdev.com/Platform/common-config/pull/11) 2014-06-27 17:03:57)

Added property to wire up Akka logger to SLF4J

### Improvements

- Akka loggers will be wired up to SLF4J by default.

## 0.6.0 ([#10](https://git.mobcastdev.com/Platform/common-config/pull/10) 2014-06-26 18:05:50)

Added Loggers trait and updated some config

### Breaking changes

- `SwaggerConfig` now uses `docsPath` instead of `specPath` and `resourcePath` to reflect the changes made in the spray-swagger library.
- `RabbitConfig` has been removed as that type has been moved into the rabbitmq-ha library.

### New features

- Added a `Loggers` trait which can be mixed into apps to configure the standard loggers.
- Added some `getXxxOption` methods to the `Config` class to make it easier to use optional settings.

## 0.5.0 ([#9](https://git.mobcastdev.com/Platform/common-config/pull/9) 2014-06-23 17:04:24)

Added a diagnostic execution context

### New features

- Added `DiagnosticExecutionContext` which can be used to wrap any `ExecutionContext` to flow MDC information between threads.

## 0.4.0 ([#8](https://git.mobcastdev.com/Platform/common-config/pull/8) 2014-06-19 13:31:45)

Added GELF (Graylog) logging

### New features

- Added Logback GELF formatter and UDP appender for sending messages to Graylog

## 0.3.2 ([#7](https://git.mobcastdev.com/Platform/common-config/pull/7) 2014-06-17 08:59:50)

Changed non-parsing URLs to URIs

### Bug fixes

- Changed database and rabbit `URL`s to `URI`s as the mysql and amqp
schemes fail to parse as URLs.
- Corrected the swagger config loading to use the string `v1` rather
than just `1`.

### Improvements

- Added tests for the config section loading to prevent silly mistakes
like the above!

## 0.3.1 ([#6](https://git.mobcastdev.com/Platform/common-config/pull/6) 2014-06-16 17:19:04)

Changed Duration to FiniteDuration

Patch to return the more specific `FiniteDuration` type rather than
`Duration` as the latter is not implicitly convertible to an Akka
timeout.

## 0.3.0 ([#5](https://git.mobcastdev.com/Platform/common-config/pull/5) 2014-06-16 10:58:58)

Added some typed config sections.

### New features

- Added some typed config objects for common settings.
- Added a `getFile` method to the `Config` class.
- Improved the README.

## 0.2.1 ([#4](https://git.mobcastdev.com/Platform/common-config/pull/4) 2014-06-05 13:09:04)

Tiny change to force rebuild, should be published to Artifactory

Patch: force version update for rebuild.

## 0.2.0 ([#2](https://git.mobcastdev.com/Platform/common-config/pull/2) 2014-05-27 17:01:23)

Added tests for config loading

#### New features

- The external configuration file can be overridden by the system
properties (e.g. `java -Dmyapp.foo.bar=10`).

#### Improvements

- The README file now explains the usage and has an example of strongly
typed config objects.

## 0.1.0 ([#1](https://git.mobcastdev.com/Platform/common-config/pull/1) 2014-05-22 15:26:14)

Added basic config support

#### New features

- Added a `Configuration` trait so that you can mix it into things that need to be configured, e.g. `class MyApp extends App with Configuration`.
- Added a `UrlConfig` implicit class to validate getting URLs from configuration.

