package com.themillhousegroup.gatsby.http

import io.gatling.http.request.builder._
import io.gatling.http.config.HttpProtocol
import io.gatling.http.action.{ HttpRequestAction, HttpActionBuilder }
import akka.actor.ActorRef
import io.gatling.core.config.Protocols
import akka.actor.ActorDSL._
import io.gatling.core.session._
import io.gatling.http.request.builder.HttpAttributes
import io.gatling.core.controller.throttle.ThrottlingProtocol
import io.gatling.http.request.builder.CommonAttributes
import com.ning.http.client.Request
import com.themillhousegroup.gatsby.actors.{ TearDown, SpinUp }
import com.themillhousegroup.gatsby.DynamicStubExchange

class GatsbyHttpRequestBuilder(commonAttributes: CommonAttributes,
    httpAttributes: HttpAttributes,
    val url: String,
    val requestName: String,
    val simulation: DynamicStubExchange) extends AbstractHttpRequestBuilder[GatsbyHttpRequestBuilder](commonAttributes, httpAttributes) {

  def newInstance(commonAttributes: CommonAttributes): GatsbyHttpRequestBuilder = new GatsbyHttpRequestBuilder(commonAttributes, httpAttributes, url, requestName, simulation)

  def newInstance(httpAttributes: HttpAttributes): GatsbyHttpRequestBuilder = new GatsbyHttpRequestBuilder(commonAttributes, httpAttributes, url, requestName, simulation)

  def request(protocol: HttpProtocol): Expression[Request] = new HttpRequestExpressionBuilder(commonAttributes, httpAttributes, protocol).build
}

class GatsbyHttpRequestWithParamsWrapper(commonAttributes: CommonAttributes,
  httpAttributes: HttpAttributes,
  formParams: List[HttpParam])(onBuild: => Unit)
    extends HttpRequestWithParamsBuilder(commonAttributes, httpAttributes, formParams) {
}

