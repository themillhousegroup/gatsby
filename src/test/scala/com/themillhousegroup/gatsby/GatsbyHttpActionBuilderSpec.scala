package com.themillhousegroup.gatsby

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import com.themillhousegroup.gatsby.stubby.RuntimeStubbing
import io.gatling.core.session.{ Expression, Session }
import io.gatling.http.request.builder.{ CommonAttributes, AbstractHttpRequestBuilder }
import com.themillhousegroup.gatsby.test.{ NextActor, ActorScope }
import akka.testkit.TestActorRef
import io.gatling.core.config.{ GatlingConfiguration, Protocols }
import io.gatling.http.config.{ HttpProtocolResponsePart, HttpProtocol }
import io.gatling.core.controller.throttle.ThrottlingProtocol
import io.gatling.core.akka.GatlingActorSystem
import io.gatling.core.validation.Success
import io.gatling.http.request.{ HttpRequestConfig, HttpRequestDef }
import com.dividezero.stubby.core.model.StubExchange

class GatsbyHttpActionBuilderSpec extends Specification with Mockito {

  // Prevent explosions when the *ActionBuilder attempts to read configuration:
  GatlingConfiguration.setUp()
  GatlingActorSystem.start

  "GatsbyHttpActionBuilder" should {
    "Cause a 200 OK stub to be built when a request is wrapped in withStubby" in new ActorScope {

      implicit val mockSimulation = mock[RuntimeStubbing]

      val mockSession = mock[Session]
      mockSession.scenarioName returns "s1"

      val requestBuilder = givenRequest("GET", "/foo", "myRequest")

      val builder = GatsbyHttpActionBuilder.withStubby(requestBuilder)

      val next = TestActorRef[NextActor]

      val mockProtocols = mock[Protocols]
      mockProtocols.getProtocol[ThrottlingProtocol] returns None
      mockProtocols.getProtocol[HttpProtocol] returns Some(HttpProtocol.DefaultHttpProtocol)

      val result = builder.build(next, mockProtocols)

      //      there was one(mockSimulation).addExchange(anyString, any[StubExchange])
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
