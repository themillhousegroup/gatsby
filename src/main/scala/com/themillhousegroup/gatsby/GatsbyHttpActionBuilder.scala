package com.themillhousegroup.gatsby

import io.gatling.http.action.{ HttpRequestAction, HttpActionBuilder }
import akka.actor.ActorRef
import io.gatling.core.config.Protocols
import akka.actor.ActorDSL._
import io.gatling.core.controller.throttle.ThrottlingProtocol
import com.themillhousegroup.gatsby.actors.{ SpinUp, TearDown }
import com.themillhousegroup.gatsby.stubby.{ RuntimeStubbing, StubExchanges }
import io.gatling.http.request.builder.AbstractHttpRequestBuilder
import com.dividezero.stubby.core.model.StubExchange
import io.gatling.core.session.Expression
import scala.collection.mutable
import com.typesafe.scalalogging.slf4j.StrictLogging

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
    simulation: RuntimeStubbing) extends HttpActionBuilder with StrictLogging {

  val stubExchanges = mutable.Buffer[Expression[StubExchange]]()
  stubExchanges += StubExchanges.buildExchangeExpression(
    requestBuilder.commonAttributes.method,
    requestBuilder.commonAttributes.urlOrURI.left.get,
    responseStatus,
    responseBody,
    responseContentType)

  /** Chain up additional stubbed responses after the "primary" one */
  def andAdditionalStubbing(method: String, url: String, responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None) = {
    logger.info(s"Additional stubbing specified: $method $url")

    import io.gatling.core.session.ExpressionWrapper
    stubExchanges += StubExchanges.buildExchangeExpression(
      method,
      ExpressionWrapper[String](url).expression,
      responseStatus,
      responseBody,
      responseContentType)
    this
  }

  def build(next: ActorRef, protocols: Protocols): ActorRef = {
    val throttled = protocols.getProtocol[ThrottlingProtocol].isDefined
    val httpRequest = requestBuilder.build(httpProtocol(protocols), throttled)

    // Build the chain of 3 actors that configure Stubby, fire the request, and de-configure Stubby:
    val tearDown = actor(new TearDown(simulation, requestBuilder.commonAttributes.requestName, next))
    val request = actor(new HttpRequestAction(httpRequest, tearDown))
    val spinUp = actor(new SpinUp(simulation, requestBuilder.commonAttributes.requestName, stubExchanges.toSeq, request))

    spinUp
  }

}
