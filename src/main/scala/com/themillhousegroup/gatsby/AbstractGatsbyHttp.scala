package com.themillhousegroup.gatsby

import io.gatling.core.session._
import io.gatling.http.request.builder._
import org.slf4j.LoggerFactory
import com.dividezero.stubby.core.model.StubRequest
import com.dividezero.stubby.core.model.StubResponse
import io.gatling.http.request.builder.HttpAttributes
import scala.Some
import com.dividezero.stubby.core.model.StubExchange

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

  def httpRequestWithParams(method: String,
    url: ExpressionAndPlainString,
    httpAttributes: HttpAttributes = new HttpAttributes(),
    formParams: List[HttpParam] = Nil): HttpRequestWithParamsBuilder = {
    logger.info(s"Configuring Dynamic Gatsby HTTP response for: $method ${url.plain}")

    // TODO: need to include CommonAttributes to allow headers to be set and/or matched.

    // TODO: map the incoming HTTPAttributes and HttpParams to Stubby's StubParams
    // to get closer matching
    simulation.addExchange(
      buildExchange(method, url.toString))

    new HttpRequestWithParamsBuilder(CommonAttributes(requestName, method, Left(url.exp)), httpAttributes, formParams)
  }

}
