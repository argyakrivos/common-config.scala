package com.blinkbox.books

import java.net.{MalformedURLException, URL}
import com.typesafe.config.Config
import com.typesafe.config.ConfigException.BadValue

/**
 * Provides classes to help with configuring applications.
 */
package object config {

  /**
   * Extends the Config class with methods that retrieve and validate URLs.
   * @param config The config class to extend.
   */
  implicit class UrlConfig(val config: Config) extends AnyVal {
    /**
     * Gets a URL with any scheme.
     * @param path The path expression.
     * @return The URI value at the requested path.
     */
    def getUrl(path: String): URL = {
      val s = config.getString(path)
      try new URL(s) catch {
        case e: MalformedURLException => throw new BadValue(config.origin, path, s"Invalid URL '$s'.")
      }
    }

    /**
     * Gets a URL with a permitted scheme.
     * @param path The path expression.
     * @param schemes The permitted schemes for the URL.
     * @return The URI value at the requested path.
     */
    def getUrl(path: String, schemes: String*): URL = {
      val url = getUrl(path)
      if (!schemes.contains(url.getProtocol)) throw new BadValue(config.origin, path, s"Invalid scheme '$url'.")
      url
    }

    /**
     * Gets a URL with an amqp scheme.
     * @param path The path expression.
     * @return The amqp URI value at the requested path.
     */
    def getAmqpUrl(path: String): URL = getUrl(path, "amqp")

    /**
     * Gets a URL with an http or https scheme.
     * @param path The path expression.
     * @return The http(s) URI value at the requested path.
     */
    def getHttpUrl(path: String): URL = getUrl(path, "http", "https")
  }

}
