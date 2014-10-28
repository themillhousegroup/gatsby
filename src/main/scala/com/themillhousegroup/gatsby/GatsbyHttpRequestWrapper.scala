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

class GatsbyHttpRequestWrapper(commonAttributes: CommonAttributes, httpAttributes: HttpAttributes) extends AbstractHttpRequestBuilder[GatsbyHttpRequestWrapper](commonAttributes, httpAttributes) {

  override def newInstance(commonAttributes: CommonAttributes) = new GatsbyHttpRequestWrapper(commonAttributes, httpAttributes)
  override def newInstance(httpAttributes: HttpAttributes) = new GatsbyHttpRequestWrapper(commonAttributes, httpAttributes)

  override def request(protocol: HttpProtocol): Expression[Request] = {
    logger.info("Intercepting request()")
    new HttpRequestExpressionBuilder(commonAttributes, httpAttributes, protocol).build
  }

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
  def withStubby(requestBuilder: AbstractHttpRequestBuilder[_]) = {
    new GatsbyHttpRequestActionBuilder(requestBuilder)
  }
}

class GatsbyHttpRequestActionBuilder(requestBuilder: AbstractHttpRequestBuilder[_]) extends HttpActionBuilder {

  private val logger = LoggerFactory.getLogger(getClass)

  logger.info("GatsbyHttpRequestActionBuilder constructed")

  def build(next: ActorRef, protocols: Protocols): ActorRef = {
    logger.info("GatsbyHttpRequestActionBuilder build()")
    val throttled = protocols.getProtocol[ThrottlingProtocol].isDefined
    val httpRequest = requestBuilder.build(httpProtocol(protocols), throttled)


    val tearDown = actor(new TearDown("blah", next))
    val request = actor(new HttpRequestAction(httpRequest, tearDown))
    val spinUp = actor(new SpinUp("blurgh", request))

    // should return spinUp...

    actor(new HttpRequestAction(httpRequest, next))
  }
}

class SpinUp(msg:String, val next:ActorRef) extends Chainable {
  println(s"spinning up $msg")
}

class TearDown(msg:String, val next:ActorRef) extends Chainable {
  println(s"tearing down $msg")
}
