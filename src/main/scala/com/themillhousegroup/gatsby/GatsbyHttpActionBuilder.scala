package com.themillhousegroup.gatsby

import io.gatling.http.action.{ HttpRequestAction, HttpActionBuilder }
import akka.actor.ActorRef
import io.gatling.core.config.Protocols
import akka.actor.ActorDSL._
import io.gatling.core.controller.throttle.ThrottlingProtocol
import com.themillhousegroup.gatsby.actors.{ SpinUp, TearDown }
import com.themillhousegroup.gatsby.stubby.{ HasLogger, RuntimeStubbing, StubExchanges }
import io.gatling.http.request.builder.AbstractHttpRequestBuilder

object GatsbyHttpActionBuilder {

  /** If you just want the given request to be responded-to with a simple 200 OK with empty body and no Content-Type, this is your method */
  def withStubby(requestBuilder: AbstractHttpRequestBuilder[_])(implicit simulation: RuntimeStubbing): GatsbyHttpActionBuilder = withStubby()(requestBuilder)(simulation)

  /** Supplying extra details about how Stubby should respond */
  def withStubby(responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None)(requestBuilder: AbstractHttpRequestBuilder[_])(implicit simulation: RuntimeStubbing): GatsbyHttpActionBuilder = {
    new GatsbyHttpActionBuilder(requestBuilder, responseStatus, responseBody, responseContentType, simulation)
  }
}

class GatsbyHttpActionBuilder(
    requestBuilder: AbstractHttpRequestBuilder[_],
    val responseStatus: Int,
    val responseBody: Option[AnyRef],
    val responseContentType: Option[String],
    simulation: RuntimeStubbing) extends HttpActionBuilder with HasLogger {

  def build(next: ActorRef, protocols: Protocols): ActorRef = {
    val throttled = protocols.getProtocol[ThrottlingProtocol].isDefined
    val httpRequest = requestBuilder.build(httpProtocol(protocols), throttled)

    val se = StubExchanges.buildExchangeExpression(requestBuilder.commonAttributes.method,
      requestBuilder.commonAttributes.urlOrURI.left.get,
      responseStatus,
      responseBody,
      responseContentType)

    // Build the chain of 3 actors that configure Stubby, fire the request, and de-configure Stubby:
    val tearDown = actor(new TearDown(simulation, requestBuilder.commonAttributes.requestName, next))
    val request = actor(new HttpRequestAction(httpRequest, tearDown))
    val spinUp = actor(new SpinUp(simulation, requestBuilder.commonAttributes.requestName, se, request))

    spinUp
  }
}
