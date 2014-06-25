# Change Log

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

