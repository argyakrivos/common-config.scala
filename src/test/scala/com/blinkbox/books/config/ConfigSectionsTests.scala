package com.blinkbox.books.config

import java.io.File
import java.net.{URI, URL}
import org.scalatest.{FunSuite, Matchers}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

class ConfigSectionsTests extends FunSuite with Matchers with Configuration {

  test("Load API config") {
    val c = ApiConfig(config, "service.test.api.public")
    assert(c.externalUrl === new URL("https://api.blinkboxbooks.com/test"))
    assert(c.localUrl === new URL("http://localhost:8080/test"))
    assert(c.timeout === FiniteDuration(10, TimeUnit.SECONDS))
  }

  test("Load auth client config") {
    val c = AuthClientConfig(config)
    assert(c.url === new URL("http://auth.blinkboxbooks.internal"))
    assert(c.keysDir === new File("./"))
  }

  test("Load database config") {
    val c = DatabaseConfig(config, "service.test.db")
    assert(c.uri === new URI("mysql://guest:guest@localhost/mydb"))
  }

}
