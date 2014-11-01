package com.themillhousegroup.gatsby.stubby

import com.dividezero.stubby.core.model.{ StubParam, StubResponse, StubRequest, StubExchange }
import io.gatling.core.session._
import com.dividezero.stubby.core.model.StubRequest
import com.dividezero.stubby.core.model.StubParam
import com.dividezero.stubby.core.model.StubResponse
import scala.Some
import com.dividezero.stubby.core.model.StubExchange

object StubExchanges {
  def buildExchange(method: String, url: String, responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None): StubExchange = {
    StubExchange(
      StubRequest(Some(method), Some(url), Nil, Nil, None),
      StubResponse(responseStatus, responseContentType.map(StubParam("Content-Type", _)).toList, responseBody))
  }

  def buildExchangeExpression(method: String,
    url: Expression[String],
    responseStatus: Int = 200,
    responseBody: Option[AnyRef] = None,
    responseContentType: Option[String] = None): Expression[StubExchange] = {
    url(_).map(u => buildExchange(method, u, responseStatus, responseBody, responseContentType))
  }
}