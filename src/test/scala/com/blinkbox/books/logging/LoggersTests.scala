package com.blinkbox.books.logging

import org.scalatest.{FunSuite, Matchers}
import com.blinkbox.books.config.Configuration

private class TestApp extends App with Configuration with Loggers

class LoggersTests extends FunSuite with Matchers {

  // these tests seem a bit sparse but there's not much else that can sensibly be tested without
  // simply copying the code that does the configuration and asserting that it's all the same --
  // that doesn't really feel like it adds much value though

  test("An app can be created with the loggers") {
    new TestApp
  }

}
