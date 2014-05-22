package com.blinkbox.books.config

import com.typesafe.config.{Config, ConfigException, ConfigFactory, ConfigParseOptions}
import java.io.File
import java.net.{URI, URISyntaxException}

/**
 * A trait that can be mixed in to load configuration in the standard blinkbox books way.
 *
 * The configuration is loaded from ''application.conf'' and ''reference.conf'' in the usual
 * way, but if there is an environment variable `CONFIG_URL` defined then config will also
 * be loaded from this and composited between the two. The following URL schemes are supported:
 *
 *  - `http` and `https`, which are ideal for production systems with a centralised configuration
 *    service.
 *  - `file`, which is ideal for  production systems using a configuration management system
 *    such as puppet to create application-specific configuration files.
 *  - `classpath`, which is intended for development or testing purposes so that you don't need
 *    any external dependencies. It's highly recommended that you use file names that won't be
 *    mistaken for production configuration, such as ''development.conf''.
 *
 * Examples:
 *
 *  - `https://config-service/dynamic.conf`
 *  - `file:///etc/my-service.conf`
 *  - `classpath:///development.conf`
 */
trait Configuration {
  private val configVar = "CONFIG_URL"
  private val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)

  /**
   * The loaded configuration.
   */
  implicit val config: Config = loadConfig

  private def loadConfig = {
    val localConfig = ConfigFactory.load
    loadExternalConfig match {
      case Some(externalConfig) => localConfig.withFallback(externalConfig)
      case None => localConfig
    }
  }

  private def loadExternalConfig: Option[Config] =
    Option(System.getenv(configVar)) map { s =>
      val uri = parseUri(s)
      uri.getScheme match {
        case _ @ ("http" | "https") => ConfigFactory.parseURL(uri.toURL, parseOptions)
        case "file" => ConfigFactory.parseFile(new File(uri), parseOptions)
        case "classpath" => ConfigFactory.parseResources(this.getClass, uri.getPath, parseOptions)
        case other => throw new ConfigException.Generic(s"$configVar has unsupported scheme '$other'.")
      }
    }

  private def parseUri(uri: String): URI =
    try new URI(uri)
    catch {
      case e: URISyntaxException => throw new ConfigException.Generic(s"$configVar has invalid value '$uri'.", e)
    }
}