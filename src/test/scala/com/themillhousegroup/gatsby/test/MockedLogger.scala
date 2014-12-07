package com.themillhousegroup.gatsby.test

import com.typesafe.scalalogging.slf4j.{ Logging, Logger }

trait MockedLogger {
  val logger = Logger(org.mockito.Mockito.mock(classOf[org.slf4j.Logger]))
}

trait MockedLogging extends Logging {
  override protected lazy val logger = Logger(org.mockito.Mockito.mock(classOf[org.slf4j.Logger]))
}
