package com.themillhousegroup.gatsby

import io.gatling.http.request.builder.{ HttpParam, HttpAttributes }
import io.gatling.core.session._
import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.http.AbstractGatsbyHttp

object GatsbyHttp {

  /**
   * Replace usages of io.gatling.http.Predef.http in your simulation files
   * with this one to get Gatsby functionality.
   */

  def gatsbyHttp(requestName: String) = {
    new GatsbyHttp(requestName, requestName)
  }

  val applicationJson = Some("application/json")
  val testXml = Some("text/xml")
  val applicationXml = Some("application/xml")
}

/**
 * Echoing the "normal" HTTP verbs as supplied by io.gatling.http.request.builder.Http,
 * but using the ExpressionAndPlainString container so we can resolve values *before*
 * the simulation is actually run.
 */
class GatsbyHttp(requestName: String, requestNameExp: Expression[String]) extends AbstractGatsbyHttp(requestName, requestNameExp) with HasLogger {

  /** The Stubby endpoint will return an empty 200 OK */
  def get(url: ExpressionAndPlainString) = httpRequest("GET", url)

  def post(url: ExpressionAndPlainString) = httpRequestWithParams("POST", url)

  def post(url: ExpressionAndPlainString,
    httpAttributes: HttpAttributes,
    formParams: List[HttpParam]) = {

    httpRequestWithParams("POST", url, httpAttributes, formParams)
  }

  def put(url: ExpressionAndPlainString) = httpRequest("PUT", url)
  def patch(url: ExpressionAndPlainString) = httpRequest("PATCH", url)
  def head(url: ExpressionAndPlainString) = httpRequest("HEAD", url)
  def delete(url: ExpressionAndPlainString) = httpRequest("DELETE", url)
  def options(url: ExpressionAndPlainString) = httpRequest("OPTIONS", url)

  /**
   * If you have many stub exchanges to set up, configure them here
   */
  //  def withAdditionalStubbing(exchanges: StubExchange*): GatsbyHttp = {
  //    exchanges.foreach { ex =>
  //      val uniqueName = s"${requestName}--${ex.request.method.get}--${ex.request.path.get}"
  //      simulation.addExchange(uniqueName, ex)
  //    }
  //    this
  //  }

  /**
   * Shortcut method when you just need one quick-and-dirty stub endpoint that returns a status, and optionally a body and a given Content-Type
   */
  //  def withAdditionalStubbing(method: String, url: String, responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None): GatsbyHttp = {
  //    withAdditionalStubbing(buildExchange(method, url, responseStatus, responseBody, responseContentType))
  //  }
}

