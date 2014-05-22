package com.blinkbox.books.config

import com.typesafe.config.{Config, ConfigException, ConfigFactory, ConfigParseOptions}
import java.io.File
import java.net.{URI, URISyntaxException}

/**
 * A trait that can be mixed in to load configuration in the standard way.
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
        case other => throw new ConfigException.Generic(s"$configVar has unsupported scheme '$other'.")
      }
    }

  private def parseUri(uri: String): URI =
    try new URI(uri)
    catch {
      case e: URISyntaxException => throw new ConfigException.Generic(s"$configVar has invalid value '$uri'.", e)
    }
}
