# common-config

Contains code to configure your Scala projects.

## Overview

The configuration is loaded from the system properties, `application.conf` and `reference.conf` in the usual way using the [Typesafe Config library](https://github.com/typesafehub/config). However, if there is an environment variable `CONFIG_URL` defined then config will also be loaded from this and override any local settings _except_ those specified in the system properties. In other words, the order of precedence for config settings is:

 - System properties (e.g. `java -Dmyapp.foo.bar=10`)
 - The configuration at `CONFIG_URL`
 - `application.conf`
 - `reference.conf`

The following URL schemes are supported:

 - `http` and `https`, which are ideal for production systems with a centralised configuration service.
 - `file`, which is ideal for  production systems using a configuration management system such as puppet to create application-specific configuration files.
 - `classpath`, which is intended for development or testing purposes so that you don't need any external dependencies. It's highly recommended that you use file names that won't be mistaken for production configuration, such as ''development.conf''.

Examples:

 - `https://config-service/dynamic.conf`
 - `file:///etc/my-service.conf`
 - `classpath:///development.conf`

## Using the library

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
      case env => throw new BadValue(config.origin, "braintree.environment", s"Unknown environment '$env'.")
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
