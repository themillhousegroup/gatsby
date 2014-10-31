package com.themillhousegroup.gatsby.http

import io.gatling.core.session._
import io.gatling.http.request.builder._
import io.gatling.http.request.builder.HttpAttributes
import com.themillhousegroup.gatsby.stubby.StubExchanges
import StubExchanges.buildExchange
import com.themillhousegroup.gatsby.{ ExpressionAndPlainString, HasLogger, DynamicStubExchange }

abstract class AbstractGatsbyHttp(requestName: String, requestNameExp: Expression[String], simulation: DynamicStubExchange) extends Http(requestNameExp) with HasLogger {

  def httpRequest(method: String, url: ExpressionAndPlainString, responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None): GatsbyHttpRequestBuilder = {
    logger.info(s"Configuring Dynamic Gatsby HTTP response for: $method ${url.plain}")

    new GatsbyHttpRequestBuilder(CommonAttributes(requestNameExp, method, Left(url.exp)), HttpAttributes(), url.plain, requestName, simulation)
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
