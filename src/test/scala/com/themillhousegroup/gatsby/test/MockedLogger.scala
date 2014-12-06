package com.themillhousegroup.gatsby.test

import com.typesafe.scalalogging.slf4j.Logger

trait MockedLogger {
  val logger = Logger(org.mockito.Mockito.mock(classOf[org.slf4j.Logger]))
}
