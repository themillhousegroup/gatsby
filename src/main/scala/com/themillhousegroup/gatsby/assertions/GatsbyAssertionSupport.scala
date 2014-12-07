package com.themillhousegroup.gatsby.assertions

import io.gatling.core.assertion.Metric
import io.gatling.core.result.reader.DataReader
import io.gatling.core.validation.Success
import org.slf4j.LoggerFactory
import com.dividezero.stubby.core.model.{ StubExchange, StubRequest }
import com.themillhousegroup.gatsby.stubby.StubbyServer
import com.themillhousegroup.gatsby.AbstractGatsbySimulation
import com.typesafe.scalalogging.slf4j.Logging

trait GatsbyAssertionSupport {
  this: AbstractGatsbySimulation =>

  lazy val stubby = new GatsbySelector(mainServer)
}

class GatsbySelector(val stubbyServer: StubbyServer) extends Logging {

  def readerToRequests(filterFn: StubRequest => Boolean)(dr: DataReader) = {
    logger.debug(s"stubbyServer.requestsSeen: ${stubbyServer.requestsSeen}")
    Success(stubbyServer.requestsSeen.filter(filterFn).size)
  }

  def readerToExchanges(filterFn: StubExchange => Boolean)(dr: DataReader) = {
    logger.debug(s"stubbyServer.exchangesSeen: ${stubbyServer.exchangesSeen}")
    Success(stubbyServer.exchangesSeen.filter(filterFn).size)
  }

  def requestsSeen = Metric(readerToRequests(_ => true), "Requests Seen")

  def requestsSeenFor(url: String) = Metric(readerToRequests(_.path.equals(Some(url))), s"Requests Seen for $url")

  def exchangesSeen = Metric(readerToExchanges(_ => true), "Exchanges Seen")

  def exchangesSeenFor(url: String) = Metric(readerToExchanges(_.request.path.equals(Some(url))), s"Exchanges Seen for $url")
}
