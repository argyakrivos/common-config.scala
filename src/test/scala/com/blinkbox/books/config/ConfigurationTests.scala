package com.blinkbox.books.config

import com.blinkbox.books.config
import com.typesafe.config.ConfigFactory
import java.io.File
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}

class ConfigurationTests extends FunSuite with BeforeAndAfterEach with Matchers {

  class Configured extends Configuration

  val originalEnvironment = System.getenv()

  override def afterEach() {
    setEnv(originalEnvironment)
    System.clearProperty("testing.conf.test")
  }

  test("Loads configuration normally when no environment variable is set") {
    val config = loadTestConfig
    config.getString("application.conf.test") should be("application")
    config.getString("reference.conf.test") should be("reference")
  }

  test("Loads config from classpath URL specified in CONFIG_URL") {
    setConfigUrl(Some("classpath:///testing.conf"))
    val config = loadTestConfig
    config.getString("application.conf.test") should be("application")
    config.getString("testing.conf.test") should be("testing")
    config.getString("reference.conf.test") should be("reference")
  }

  test("Loads config from file URL specified in CONFIG_URL") {
    setConfigUrl(Some(resourceFile("testing.conf")))
    val config = loadTestConfig
    config.getString("application.conf.test") should be("application")
    config.getString("testing.conf.test") should be("testing")
    config.getString("reference.conf.test") should be("reference")
  }

  test("Config files are merged with the external one taking precedence") {
    setConfigUrl(Some("classpath:///testing.conf"))
    val config = loadTestConfig
    config.getString("key1") should be("testing")
    config.getString("key2") should be("application")
    config.getString("key3") should be("reference")
  }

  test("External config can be overridden by system properties") {
    setConfigUrl(Some("classpath:///testing.conf"))
    System.setProperty("testing.conf.test", "overridden!")
    val config = loadTestConfig
    config.getString("testing.conf.test") should be("overridden!")
  }

  test("Get MapOption") {
    setConfigUrl(Some(resourceFile("testing.conf")))
    val config = loadTestConfig
    val map = config.getConfigObjectOption("map").get
    assert( "value1" == map.get("key1").unwrapped())
    assert( "value2" == map.get("key2").unwrapped())
  }

  // TODO: Could test HTTP loading using URLStreamHandlerFactory, but is it worth the effort?

  private def setConfigUrl(url: Option[String]) = {
    val newEnv = new java.util.HashMap[String, String](System.getenv())
    url.foreach(newEnv.put("CONFIG_URL", _))
    setEnv(newEnv)
  }

  // dirty dirty hack to allow setting environment variables
  private def setEnv(newEnv: java.util.Map[String, String]) {
    val classes = classOf[java.util.Collections].getDeclaredClasses
    val cl = classes.filter(_.getName == "java.util.Collections$UnmodifiableMap").head
    val field = cl.getDeclaredField("m")
    field.setAccessible(true)
    val map = field.get(System.getenv).asInstanceOf[java.util.Map[String, String]]
    map.clear()
    map.putAll(newEnv)
  }

  private def resourceFile(filename: String) = getClass.getClassLoader.getResource(filename).toString

  private def loadTestConfig = {
    ConfigFactory.invalidateCaches() // ensure we're loading afresh
    new Configured().config
  }
}
