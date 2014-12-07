package com.themillhousegroup.gatsby.test

import com.typesafe.scalalogging.slf4j.{ StrictLogging, Logging, Logger }

trait MockedLogging extends Logging {
  override protected lazy val logger = Logger(org.mockito.Mockito.mock(classOf[org.slf4j.Logger]))
}

trait MockedStrictLogging extends StrictLogging {
  override protected val logger = Logger(org.mockito.Mockito.mock(classOf[org.slf4j.Logger]))
}
