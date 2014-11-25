package com.themillhousegroup.gatsby

import org.specs2.mock.Mockito
import org.specs2.mutable.{ After, Specification }
import com.themillhousegroup.gatsby.stubby.RuntimeStubbing
import io.gatling.core.session.{ Expression, Session }
import io.gatling.http.request.builder.{ CommonAttributes, AbstractHttpRequestBuilder }
import com.themillhousegroup.gatsby.test.ActorScope
import io.gatling.core.config.{ GatlingConfiguration, Protocols }
import io.gatling.http.config.{ HttpProtocolResponsePart, HttpProtocol }
import io.gatling.core.controller.throttle.ThrottlingProtocol
import io.gatling.core.akka.GatlingActorSystem
import io.gatling.core.validation.Success
import io.gatling.http.request.{ HttpRequestConfig, HttpRequestDef }
import scala.concurrent.Future
import akka.actor.ActorRef

class GatsbyHttpActionBuilderSpec extends Specification with Mockito {

  // Prevent explosions when the *ActionBuilder attempts to read configuration:
  GatlingConfiguration.setUp()
  GatlingActorSystem.start

  "GatsbyHttpActionBuilder" should {
    "Cause a 200 OK stub to be built when a request is wrapped in withStubby" in new ActorScope {

      implicit val mockSimulation = mock[RuntimeStubbing]

      val mockSession = Session("s1", "me")
      mockSimulation.acquireLock(anyString) returns Future.successful(true)

      val requestBuilder = givenRequest("GET", "/foo", "myRequest")

      val builder = GatsbyHttpActionBuilder.withStubby(requestBuilder)

      val next = mock[ActorRef]

      val mockProtocols = mock[Protocols]
      mockProtocols.getProtocol[ThrottlingProtocol] returns None
      mockProtocols.getProtocol[HttpProtocol] returns Some(HttpProtocol.DefaultHttpProtocol)

      builder.build(next, mockProtocols)
    }
  }

  def givenRequest(method: String, url: String, requestName: String) = {
    val mockRequestBuilder = mock[AbstractHttpRequestBuilder[_]]
    val mockCommonAttribs = mock[CommonAttributes]
    val mockUrlExpression = mock[Expression[String]]
    val mockRequestName = mock[Expression[String]]
    val mockRequestDef = mock[HttpRequestDef]
    val mockRequestConfig = mock[HttpRequestConfig]
    val mockProtocol = mock[HttpProtocol]
    val mockResponsePart = mock[HttpProtocolResponsePart]

    mockRequestBuilder.commonAttributes returns mockCommonAttribs
    mockCommonAttribs.method returns method
    mockCommonAttribs.urlOrURI returns Left(mockUrlExpression)

    mockUrlExpression.apply(any[Session]) returns Success(url)

    mockCommonAttribs.requestName returns mockRequestName
    mockRequestName.apply(any[Session]) returns Success(requestName)

    mockRequestBuilder.build(any[HttpProtocol], any[Boolean]) returns mockRequestDef

    mockRequestDef.requestName returns mockRequestName
    mockRequestDef.config returns mockRequestConfig

    mockRequestConfig.checks returns List()
    mockRequestConfig.protocol returns mockProtocol
    mockProtocol.responsePart returns mockResponsePart

    mockRequestBuilder
  }
}
