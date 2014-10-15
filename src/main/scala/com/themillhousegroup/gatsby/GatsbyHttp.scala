package com.themillhousegroup.gatsby

import io.gatling.core.session._
import org.slf4j.LoggerFactory
import io.gatling.http.request.builder.{ HttpParam, HttpAttributes }

object GatsbyHttp {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def gatsbyHttp(requestName: Expression[String])(implicit simulation: CanAddStubExchanges) = {
    logger.info(s"Using Gatsby to generate responses for ${simulation.getClass.getSimpleName}")
    new GatsbyHttp(requestName, simulation)
  }
}

/**
 * By declaring a test endpoint with one of these "url:ExpressionAndPlainString" methods,
 * you're indicating that you'd like Gatsby to set
 * up Stubby endpoints for each one.
 */
class GatsbyHttp(requestName: Expression[String], simulation: CanAddStubExchanges) extends AbstractGatsbyHttp(requestName, simulation) {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def get(url: ExpressionAndPlainString) = httpRequest("GET", url)
  def post(url: ExpressionAndPlainString) = httpRequestWithParams("POST", url)

  def post(url: ExpressionAndPlainString,
    httpAttributes: HttpAttributes,
    formParams: List[HttpParam]) = httpRequestWithParams("POST", url, httpAttributes, formParams)

  def put(url: ExpressionAndPlainString) = httpRequest("PUT", url)
  def patch(url: ExpressionAndPlainString) = httpRequest("PATCH", url)
  def head(url: ExpressionAndPlainString) = httpRequest("HEAD", url)
  def delete(url: ExpressionAndPlainString) = httpRequest("DELETE", url)
  def options(url: ExpressionAndPlainString) = httpRequest("OPTIONS", url)

}
