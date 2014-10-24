package com.themillhousegroup.gatsby

import io.gatling.core.session._
import io.gatling.http.request.builder._
import org.slf4j.LoggerFactory
import com.dividezero.stubby.core.model.{ StubParam, StubRequest, StubResponse, StubExchange }
import io.gatling.http.request.builder.HttpAttributes
import scala.Some

abstract class AbstractGatsbyHttp(requestName: String, requestNameExp: Expression[String], simulation: DynamicStubExchange) extends Http(requestNameExp) {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  protected def buildExchange(method: String, url: String, responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None): StubExchange = {
    StubExchange(
      StubRequest(Some(method), Some(url), Nil, Nil, None),
      StubResponse(responseStatus, responseContentType.map(StubParam("Content-Type", _)).toList, responseBody))
  }

  def httpRequest(method: String, url: ExpressionAndPlainString): HttpRequestBuilder = {
    logger.info(s"Configuring Dynamic Gatsby HTTP response for: $method ${url.plain}")
    simulation.addExchange(requestName)(buildExchange(method, url.plain))

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

    new GatsbyHttpRequestWithParamsWrapper(
      //new HttpRequestWithParamsBuilder(
      CommonAttributes(requestNameExp, method, Left(url.exp)), httpAttributes, formParams)({
      logger.info("Running onBuild")
      simulation.addExchange(requestName)(buildExchange(method, url.plain))
    })

    //)
  }

}
