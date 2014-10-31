package com.themillhousegroup.gatsby

import org.specs2.mutable.Specification
import com.themillhousegroup.gatsby.GatsbyHttp.gatsbyHttp
import com.themillhousegroup.gatsby.GatsbyHttpActionBuilder.withStubby
import org.specs2.mock.Mockito
import io.gatling.core.Predef.stringToExpression
import com.themillhousegroup.gatsby.GatsbyImplicits.s2eps
import com.dividezero.stubby.core.model.StubExchange
import io.gatling.http.request.builder.HttpAttributes
import io.gatling.http.request.StringBody
import io.gatling.core.config.Protocols
import akka.actor.ActorRef
import io.gatling.core.session.Session
import io.gatling.http.config.HttpProtocol

class GatsbyHttpSpec extends Specification with Mockito {

  // Refactor of GatsbyHttp vs GatsbyHttpActionBuilder has made this test redundant

  //  "GatsbyHTTP" should {
  //    "Allow a GET to be defined, that results in Stubby getting configured" in {
  //
  //      implicit val mockStubbyBackend = mock[DynamicStubExchange]
  //
  //      val mockSession = mock[Session]
  //      mockSession.scenarioName returns "s1"
  //
  //      val mockProtocols = mock[Protocols]
  //      mockProtocols.getProtocol[HttpProtocol] returns Some(HttpProtocol.DefaultHttpProtocol)
  //
  //      val req = gatsbyHttp("requestName").get("/foo")
  //
  //      //      withStubby(req).build(mock[ActorRef], mockProtocols).tell(mockSession, mock[ActorRef])
  //
  //      there was one(mockStubbyBackend).addExchange(org.mockito.Matchers.eq("requestName"), any[StubExchange])
  //    }
  //
  //    "Allow a POST to be defined, that results in Stubby getting configured" in {
  //
  //      implicit val mockStubbyBackend = mock[DynamicStubExchange]
  //
  //      gatsbyHttp("requestName").post("/foo")
  //
  //      there was one(mockStubbyBackend).addExchange(org.mockito.Matchers.eq("requestName"), any[StubExchange])
  //    }
  //
  //    "Allow a POST with a body to be defined, that results in Stubby getting configured" in {
  //
  //      implicit val mockStubbyBackend = mock[DynamicStubExchange]
  //
  //      // TODO no place to set headers yet!
  //
  //      val body = """ { "json" : true } """
  //
  //      gatsbyHttp("requestName").post("/foo", HttpAttributes(body = Some(StringBody(body))), Nil)
  //
  //      there was one(mockStubbyBackend).addExchange(org.mockito.Matchers.eq("requestName"), any[StubExchange])
  //    }
  //  }
}
