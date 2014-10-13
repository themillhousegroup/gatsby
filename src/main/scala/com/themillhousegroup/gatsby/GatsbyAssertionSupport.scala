package com.themillhousegroup.gatsby

import io.gatling.core.assertion.Metric
import io.gatling.core.result.reader.DataReader
import io.gatling.core.validation.Success
import org.slf4j.LoggerFactory
import com.dividezero.stubby.core.model.StubRequest

trait GatsbyAssertionSupport {
  this: HasStubbyServer =>

  lazy val stubby = new GatsbySelector(this.stubbyServer)
}

class GatsbySelector(val stubbyServer: StubbyServer) {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def readerToRequests(filterFn: StubRequest => Boolean)(dr: DataReader) = {
    logger.debug(s"stubbyServer.requestsSeen: ${stubbyServer.requestsSeen}")
    Success(stubbyServer.requestsSeen.filter(filterFn).size)
  }

  def requestsSeen = Metric(readerToRequests(_ => true), "Requests Seen")

  def requestsSeenFor(url: String) = Metric(readerToRequests(_.path.equals(Some(url))), s"Requests Seen for $url")
}
