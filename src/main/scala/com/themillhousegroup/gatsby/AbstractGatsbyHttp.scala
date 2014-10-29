package com.themillhousegroup.gatsby

import io.gatling.core.session._
import io.gatling.http.request.builder._
import org.slf4j.LoggerFactory
import io.gatling.http.request.builder.HttpAttributes
import com.themillhousegroup.gatsby.StubExchanges.buildExchange

abstract class AbstractGatsbyHttp(requestName: String, requestNameExp: Expression[String], simulation: DynamicStubExchange) extends Http(requestNameExp) {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def httpRequest(method: String, url: ExpressionAndPlainString, responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None): GatsbyHttpRequestWrapper = {
    logger.info(s"Configuring Dynamic Gatsby HTTP response for: $method ${url.plain}")

    new GatsbyHttpRequestWrapper(CommonAttributes(requestNameExp, method, Left(url.exp)), HttpAttributes(), url.plain, requestName, simulation)
  }

  def httpRequestWithParams(method: String,
    url: ExpressionAndPlainString,
    httpAttributes: HttpAttributes = new HttpAttributes(),
    formParams: List[HttpParam] = Nil): HttpRequestWithParamsBuilder = {
    logger.info(s"Configuring Dynamic Gatsby HTTP response for: $method ${url.plain}")
    simulation.addExchange(requestName, buildExchange(method, url.plain))

    // TODO: need to include CommonAttributes to allow headers to be set and/or matched.

    // TODO: map the incoming HTTPAttributes and HttpParams to Stubby's StubParams
    // to get closer matching

    //    new GatsbyHttpRequestWithParamsWrapper(
    //      //new HttpRequestWithParamsBuilder(
    //      CommonAttributes(requestNameExp, method, Left(url.exp)), httpAttributes, formParams)({
    //      logger.info("Running onBuild")
    //      simulation.addExchange(requestName, buildExchange(method, url.plain))
    //    })

    //)

    new HttpRequestWithParamsBuilder(CommonAttributes(requestNameExp, method, Left(url.exp)), httpAttributes, formParams)
  }

}
