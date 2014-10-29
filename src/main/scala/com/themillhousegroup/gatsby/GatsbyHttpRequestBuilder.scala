package com.themillhousegroup.gatsby

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
import com.themillhousegroup.gatsby.actors.{TearDown, SpinUp}

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

object GatsbyHttpActionBuilder {

  /** If you just want the given request to be responded-to with a simple 200 OK with empty body and no Content-Type, this is your method */
  def withStubby(requestBuilder: GatsbyHttpRequestBuilder): GatsbyHttpActionBuilder = withStubby()(requestBuilder)

  /** Supplying extra details about how Stubby should respond */
  def withStubby(responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None)(requestBuilder: GatsbyHttpRequestBuilder): GatsbyHttpActionBuilder = {
    new GatsbyHttpActionBuilder(requestBuilder, responseStatus, responseBody, responseContentType)
  }
}

class GatsbyHttpActionBuilder(requestBuilder: GatsbyHttpRequestBuilder,
    responseStatus: Int = 200,
    responseBody: Option[AnyRef] = None,
    responseContentType: Option[String]) extends HttpActionBuilder with HasLogger {

  def build(next: ActorRef, protocols: Protocols): ActorRef = {
    val throttled = protocols.getProtocol[ThrottlingProtocol].isDefined
    val httpRequest = requestBuilder.build(httpProtocol(protocols), throttled)

    val se = StubExchanges.buildExchange(requestBuilder.commonAttributes.method,
      requestBuilder.url,
      responseStatus,
      responseBody,
      responseContentType)

    // Build the chain of 3 actors that configure Stubby, fire the request, and de-configure Stubby:
    val tearDown = actor(new TearDown(requestBuilder.simulation, requestBuilder.requestName, next))
    val request = actor(new HttpRequestAction(httpRequest, tearDown))
    val spinUp = actor(new SpinUp(requestBuilder.simulation, requestBuilder.requestName, se, request))

    spinUp
  }
}




