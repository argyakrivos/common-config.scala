package com.blinkbox.books

import com.typesafe.config.Config
import com.typesafe.config.ConfigException.BadValue
import java.io.File
import java.net.{MalformedURLException, URL}

/**
 * Provides classes to help with configuring applications.
 */
package object config {

  /**
   * Extends the Config class with additional loading methods.
   *
   * @param config The config class to extend.
   */
  implicit class RichConfig(val config: Config) extends AnyVal {

    /**
     * Gets a file or directory.
     * @param path The path expression.
     * @param validate A function to validate that the file is acceptable.
     * @return The file object.
     */
    def getFile(path: String, validate: File => Boolean = _ => true): File = {
      val file = new File(config.getString(path))
      if (!validate(file)) throw new BadValue(config.origin, path, s"Path '${file.getAbsolutePath}' is invalid.")
      file
    }

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
