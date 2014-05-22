package com.blinkbox.books.config

import com.typesafe.config.{Config, ConfigException, ConfigFactory, ConfigParseOptions}
import java.io.File
import java.net.{URISyntaxException, MalformedURLException, URI}

/**
 * A trait that can be mixed in to load configuration in the standard way.
 */
trait Configuration {

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

  private def loadExternalConfig: Option[Config] = {
    val configVar = "CONFIG_URL"
    Option(System.getenv(configVar)) flatMap { url =>
      val uri = try new URI(url) catch {
        case e @ (_: MalformedURLException | _: URISyntaxException) => throw new ConfigException.BugOrBroken(s"$configVar has invalid value '$url'.", e)
      }
      val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)
      uri.getScheme match {
        case _ @ ("http" | "https") => Some(ConfigFactory.parseURL(uri.toURL, parseOptions))
        case "file" => Some(ConfigFactory.parseFile(new File(uri), parseOptions))
        case _ => throw new ConfigException.BugOrBroken(s"$configVar has unsupported scheme '${uri.getScheme}'.")
      }
    }
  }
}
