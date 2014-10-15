package com.themillhousegroup.gatsby

import org.specs2.mutable.Specification
import com.themillhousegroup.gatsby.GatsbyHttp.gatsbyHttp
import org.specs2.mock.Mockito
import io.gatling.core.Predef.stringToExpression
import com.themillhousegroup.gatsby.GatsbyImplicits.s2eps
import com.dividezero.stubby.core.model.StubExchange
import io.gatling.http.request.builder.HttpAttributes
import io.gatling.http.request.StringBody

class GatsbyHttpSpec extends Specification with Mockito {

  "GatsbyHTTP" should {
    "Allow a GET to be defined, that results in Stubby getting configured" in {

      implicit val mockStubbyBackend = mock[CanAddStubExchanges]

      gatsbyHttp("requestName").get("/foo")

      there was one(mockStubbyBackend).addExchange(any[StubExchange])
    }

    "Allow a POST to be defined, that results in Stubby getting configured" in {

      implicit val mockStubbyBackend = mock[CanAddStubExchanges]

      gatsbyHttp("requestName").post("/foo")

      there was one(mockStubbyBackend).addExchange(any[StubExchange])
    }

    "Allow a POST with a body to be defined, that results in Stubby getting configured" in {

      implicit val mockStubbyBackend = mock[CanAddStubExchanges]

      // TODO no place to set headers yet!

      val body = """ { "json" : true } """

      gatsbyHttp("requestName").post("/foo", HttpAttributes(body = Some(StringBody(body))), Nil)

      there was one(mockStubbyBackend).addExchange(any[StubExchange])
    }
  }
}
