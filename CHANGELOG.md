Change Log


## 0.1.0 ([#1](https://git.mobcastdev.com/Platform/common-config/pull/1) 2014-05-22 15:26:14)

Added basic config support

#### New features

- Added a `Configuration` trait so that you can mix it into things that need to be configured, e.g. `class MyApp extends App with Configuration`.
- Added a `UrlConfig` implicit class to validate getting URLs from configuration.

