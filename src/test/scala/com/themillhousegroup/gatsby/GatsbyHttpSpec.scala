package com.themillhousegroup.gatsby

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

class GatsbyHttpSpec extends Specification with Mockito {

  // Refactor of GatsbyHttp vs GatsbyHttpActionBuilder has made this test redundant

  //  "GatsbyHTTP" should {
  //    "Allow a GET to be defined, that results in Stubby getting configured" in {
  //
  //      implicit val mockStubbyBackend = mock[RuntimeStubbing]
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
  //      implicit val mockStubbyBackend = mock[RuntimeStubbing]
  //
  //      gatsbyHttp("requestName").post("/foo")
  //
  //      there was one(mockStubbyBackend).addExchange(org.mockito.Matchers.eq("requestName"), any[StubExchange])
  //    }
  //
  //    "Allow a POST with a body to be defined, that results in Stubby getting configured" in {
  //
  //      implicit val mockStubbyBackend = mock[RuntimeStubbing]
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
