package com.blinkbox.books.config

import java.io.File
import java.net.{URI, URL}
import java.util.concurrent.TimeUnit

import com.typesafe.config.Config
import com.typesafe.config.ConfigException.BadValue

import scala.concurrent.duration._

case class ApiConfig(externalUrl: URL, localUrl: URL, timeout: FiniteDuration)
case class AuthClientConfig(url: URL, keysDir: File) {
  val sessionUrl = new URL(url, "session")
}
case class DatabaseConfig(uri: URI) {
  val host = if (uri.getHost != null) uri.getHost else throw new BadValue("db.url", "Host is missing or it's incorrect.")
  val port = if (uri.getPort != -1) Some(uri.getPort) else None
  val Array(user, pass) = if (uri.getUserInfo != null) {
    uri.getUserInfo.split(":") match {
      case x @ Array(u, p) => x
      case _ => throw new BadValue("db.url", s"Username and password information is not well-formed: '${uri.getUserInfo}'.")
    }
  } else throw new BadValue("db.url", "Username and password information is missing.")
  val db = if (uri.getPath != null && uri.getPath != "") {
    if (uri.getPath.count(_ == '/') == 1) uri.getPath.stripPrefix("/")
    else throw new BadValue("db.url", s"Database name is not well-formed: '${uri.getPath}'.")
  } else throw new BadValue("db.url", "Database name is missing.")
  val dbProperties = if (uri.getQuery != null) Some(uri.getQuery) else None
  val jdbcUrl = s"jdbc:${uri.getScheme}://$host${port.map(":" + _).getOrElse("")}/$db${dbProperties.map("?" + _).getOrElse("")}"
}
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