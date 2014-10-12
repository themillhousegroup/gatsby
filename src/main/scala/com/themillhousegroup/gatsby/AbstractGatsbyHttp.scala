package com.themillhousegroup.gatsby

import io.gatling.core.session._
import scala.Some
import io.gatling.http.request.builder.{ HttpRequestWithParamsBuilder, HttpRequestBuilder, Http }
import org.slf4j.LoggerFactory
import com.dividezero.stubby.core.model.{ StubResponse, StubRequest, StubExchange }

abstract class AbstractGatsbyHttp(requestName: Expression[String], simulation: CanAddStubExchanges) extends Http(requestName) {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  protected def buildExchange(method: String, url: String, responseStatus: Int = 200, responseBody: Option[AnyRef] = None): StubExchange = {
    StubExchange(StubRequest(Some(method), Some(url), Nil, Nil, None),
      StubResponse(responseStatus, Nil, responseBody))
  }

  def httpRequest(method: String, url: ExpressionAndPlainString): HttpRequestBuilder = {
    logger.info(s"Configuring Dynamic Gatsby HTTP response for: $method ${url.plain}")
    simulation.addExchange(
      buildExchange(method, url.plain))

    httpRequest(method, Left(url.exp))
  }

  def httpRequestWithParams(method: String, url: ExpressionAndPlainString): HttpRequestWithParamsBuilder = {
    logger.info(s"Configuring Dynamic Gatsby HTTP response for: $method ${url.plain}")
    simulation.addExchange(
      buildExchange(method, url.toString))

    httpRequestWithParams(method, Left(url.exp))
  }

}
