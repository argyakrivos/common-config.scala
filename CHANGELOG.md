# Change Log

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

