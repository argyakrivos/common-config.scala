package com.blinkbox.books.config

import com.typesafe.config.Config
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

case class ApiConfig(externalUrl: URL, localUrl: URL, timeout: Duration)
case class AuthClientConfig(url: URL, keysDir: File) {
  val sessionUrl = new URL(url, "session")
}
case class DatabaseConfig(url: URL)
case class RabbitConfig(url: URL)
case class SwaggerConfig(baseUrl: URL, specPath: String, resourcePath: String)

object ApiConfig {
  def apply(config: Config, prefix: String): ApiConfig = ApiConfig(
    config.getHttpUrl(s"$prefix.externalUrl"),
    config.getHttpUrl(s"$prefix.localUrl"),
    config.getDuration(s"$prefix.timeout", TimeUnit.MILLISECONDS).millis)
}

object AuthClientConfig {
  def apply(config: Config): AuthClientConfig = AuthClientConfig(
    config.getHttpUrl("service.auth.api.internalUrl"),
    config.getFile("client.auth.keysDir", f => f.isDirectory && f.exists))
}

object DatabaseConfig {
  def apply(config: Config, prefix: String): DatabaseConfig = DatabaseConfig(
    config.getUrl(s"$prefix.url"))
}

object RabbitConfig {
  def apply(config: Config): RabbitConfig = RabbitConfig(config.getUrl("rabbitmq.url"))
}

object SwaggerConfig {
  def apply(config: Config, version: Int): SwaggerConfig = SwaggerConfig(
    config.getHttpUrl(s"swagger.$version.baseUrl"),
    config.getString(s"swagger.$version.specPath"),
    config.getString(s"swagger.$version.resourcePath"))
}