package com.blinkbox.books.config

import com.typesafe.config.Config
import java.io.File
import java.net.{URI, URL}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

case class ApiConfig(externalUrl: URL, localUrl: URL, timeout: FiniteDuration)
case class AuthClientConfig(url: URL, keysDir: File) {
  val sessionUrl = new URL(url, "session")
}
case class DatabaseConfig(uri: URI)
case class SwaggerConfig(baseUrl: URL, docsPath: String)

object ApiConfig {
  def apply(config: Config, prefix: String): ApiConfig = ApiConfig(
    config.getHttpUrl(s"$prefix.externalUrl"),
    config.getHttpUrl(s"$prefix.localUrl"),
    config.getDuration(s"$prefix.timeout", TimeUnit.MILLISECONDS).millis)
}

object AuthClientConfig {
  def apply(config: Config): AuthClientConfig = AuthClientConfig(
    config.getHttpUrl("service.auth.api.internalUrl"),
    config.getFile("client.auth.keysDir", _.isDirectory))
}

object DatabaseConfig {
  def apply(config: Config, prefix: String): DatabaseConfig = DatabaseConfig(
    config.getUri(s"$prefix.url"))
}

object SwaggerConfig {
  def apply(config: Config, version: Int): SwaggerConfig = SwaggerConfig(
    config.getHttpUrl(s"swagger.v$version.baseUrl"),
    config.getString(s"swagger.v$version.docsPath"))
}