package com.themillhousegroup.gatsby

import io.gatling.http.request.builder._
import io.gatling.http.config.HttpProtocol
import io.gatling.http.request.HttpRequestDef
import io.gatling.http.request.builder.HttpAttributes
import io.gatling.http.request.builder.CommonAttributes
import io.gatling.http.request.HttpRequestDef
import io.gatling.http.action.{ HttpRequestActionBuilder, HttpRequestAction, HttpActionBuilder }
import akka.actor.ActorRef
import io.gatling.core.config.Protocols
import io.gatling.core.controller.throttle.ThrottlingProtocol
import akka.actor.ActorDSL._
import io.gatling.http.request.builder.HttpAttributes
import io.gatling.core.controller.throttle.ThrottlingProtocol
import io.gatling.http.request.builder.CommonAttributes
import io.gatling.http.request.HttpRequestDef
import org.slf4j.LoggerFactory
import io.gatling.core.session._
import io.gatling.http.request.builder.HttpAttributes
import io.gatling.core.controller.throttle.ThrottlingProtocol
import io.gatling.http.request.builder.CommonAttributes
import io.gatling.http.request.HttpRequestDef
import com.ning.http.client.Request
import io.gatling.core.action.Chainable
import com.dividezero.stubby.core.model.StubExchange
import io.gatling.core.validation.Validation

class GatsbyHttpRequestWrapper(commonAttributes: CommonAttributes,
    httpAttributes: HttpAttributes,
    val url: String,
    val requestName: String,
    val simulation: DynamicStubExchange) extends AbstractHttpRequestBuilder[GatsbyHttpRequestWrapper](commonAttributes, httpAttributes) {

  def newInstance(commonAttributes: CommonAttributes): GatsbyHttpRequestWrapper = new GatsbyHttpRequestWrapper(commonAttributes, httpAttributes, url, requestName, simulation)

  def newInstance(httpAttributes: HttpAttributes): GatsbyHttpRequestWrapper = new GatsbyHttpRequestWrapper(commonAttributes, httpAttributes, url, requestName, simulation)

  def request(protocol: HttpProtocol): Expression[Request] = new HttpRequestExpressionBuilder(commonAttributes, httpAttributes, protocol).build
}

class GatsbyHttpRequestWithParamsWrapper(commonAttributes: CommonAttributes,
  httpAttributes: HttpAttributes,
  formParams: List[HttpParam])(onBuild: => Unit)
    extends HttpRequestWithParamsBuilder(commonAttributes, httpAttributes, formParams) {

  override def build(protocol: HttpProtocol, throttled: Boolean): HttpRequestDef = {
    logger.info("Building NOW!")
    onBuild
    super.build(protocol, throttled)
  }
}

object GatsbyHttpRequestActionBuilder {

  /** If you just want the given request to be responded-to with a simple 200 OK with empty body and no Content-Type, this is your method */
  def withStubby(requestBuilder: GatsbyHttpRequestWrapper): GatsbyHttpRequestActionBuilder = withStubby()(requestBuilder)

  /** Supplying extra details about how Stubby should respond */
  def withStubby(responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None)(requestBuilder: GatsbyHttpRequestWrapper): GatsbyHttpRequestActionBuilder = {
    new GatsbyHttpRequestActionBuilder(requestBuilder, responseStatus, responseBody, responseContentType)
  }
}

class GatsbyHttpRequestActionBuilder(requestBuilder: GatsbyHttpRequestWrapper,
    responseStatus: Int = 200,
    responseBody: Option[AnyRef] = None,
    responseContentType: Option[String]) extends HttpActionBuilder {

  private val logger = LoggerFactory.getLogger(getClass)

  logger.info("GatsbyHttpRequestActionBuilder constructed")

  def build(next: ActorRef, protocols: Protocols): ActorRef = {
    logger.info("GatsbyHttpRequestActionBuilder build()")
    val throttled = protocols.getProtocol[ThrottlingProtocol].isDefined
    val httpRequest = requestBuilder.build(httpProtocol(protocols), throttled)

    val se = StubExchanges.buildExchange(requestBuilder.commonAttributes.method,
      requestBuilder.url,
      responseStatus,
      responseBody,
      responseContentType)

    val tearDown = actor(new TearDown(requestBuilder.simulation, requestBuilder.requestName, next))
    val request = actor(new HttpRequestAction(httpRequest, tearDown))
    val spinUp = actor(new SpinUp(requestBuilder.simulation, requestBuilder.requestName, se, request))

    spinUp
  }
}

class SpinUp(val simulation: DynamicStubExchange, val requestName: String, val se: StubExchange, val next: ActorRef) extends Chainable {

  def execute(session: Session): Unit = {

    println(s"spinning up auto-response to $requestName for scenario: ${session.scenarioName}")
    simulation.acquireLock(requestName).map { ready =>
      simulation.addExchange(requestName, se)
      next ! session
    }

  }
}

class TearDown(val simulation: DynamicStubExchange, val requestName: String, val next: ActorRef) extends Chainable {

  def execute(session: Session): Unit = {
    println(s"tearing down $requestName after scenario: ${session.scenarioName}")
    simulation.removeExchange(requestName)
    simulation.releaseLock(requestName)
    next ! session
  }
}
