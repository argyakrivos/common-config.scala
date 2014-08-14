package com.blinkbox.books.config

import java.io.File
import java.net.{URI, URL}
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigException.BadValue
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.duration.FiniteDuration

class ConfigSectionsTests extends FunSuite with Matchers with Configuration {

  test("Load API config") {
    val c = ApiConfig(config, "service.test.api.public")
    assert(c.externalUrl == new URL("https://api.blinkboxbooks.com/test"))
    assert(c.localUrl == new URL("http://localhost:8080/test"))
    assert(c.timeout == FiniteDuration(10, TimeUnit.SECONDS))
  }

  test("Load auth client config") {
    val c = AuthClientConfig(config)
    assert(c.url == new URL("http://auth.blinkboxbooks.internal"))
    assert(c.keysDir == new File("./"))
  }

  test("Load database config") {
    val c = DatabaseConfig(config, "service.test.db")
    assert(c.uri == new URI("mysql://guest:guest@localhost/mydb"))
    assert(c.host == "localhost")
    assert(c.port == None)
    assert(c.user == "guest")
    assert(c.pass == "guest")
    assert(c.db == "mydb")
    assert(c.dbProperties == None)
    assert(c.jdbcUrl == "jdbc:mysql://localhost/mydb")
  }

  test("Load database config with db properties") {
    val c = DatabaseConfig(config, "service.test2.db")
    assert(c.uri == new URI("mysql://guest:guest@localhost:3306/mydb?zeroDateTimeBehavior=convertToNull"))
    assert(c.host == "localhost")
    assert(c.port == Some(3306))
    assert(c.user == "guest")
    assert(c.pass == "guest")
    assert(c.db == "mydb")
    assert(c.dbProperties == Some("zeroDateTimeBehavior=convertToNull"))
    assert(c.jdbcUrl == "jdbc:mysql://localhost:3306/mydb?zeroDateTimeBehavior=convertToNull")
  }

  test("Fail to load database config without host") {
    val thrown = intercept[BadValue] {
      DatabaseConfig(config, "service.test3.db")
    }
    assert(thrown.getMessage == "Invalid value at 'db.url': Host is missing or it's incorrect.")
  }

  test("Fail to load database config without user information") {
    val thrown = intercept[BadValue] {
      DatabaseConfig(config, "service.test4.db")
    }
    assert(thrown.getMessage == "Invalid value at 'db.url': Username and password information is missing.")
  }

  test("Fail to load database config with invalid user information") {
    val thrown = intercept[BadValue] {
      DatabaseConfig(config, "service.test5.db")
    }
    assert(thrown.getMessage == "Invalid value at 'db.url': Username and password information is not well-formed: 'guest:guest:guest'.")
  }

  test("Fail to load database config without database") {
    val thrown = intercept[BadValue] {
      DatabaseConfig(config, "service.test6.db")
    }
    assert(thrown.getMessage == "Invalid value at 'db.url': Database name is missing.")
  }

  test("Fail to load database config with invalid database") {
    val thrown = intercept[BadValue] {
      DatabaseConfig(config, "service.test7.db")
    }
    assert(thrown.getMessage == "Invalid value at 'db.url': Database name is not well-formed: '/mydb/woah'.")
  }
}
