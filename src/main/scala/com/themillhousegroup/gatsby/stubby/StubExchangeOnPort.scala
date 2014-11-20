package com.themillhousegroup.gatsby.stubby

import com.dividezero.stubby.core.model.{ StubResponse, StubRequest, StubExchange }

object StubExchangeOnPort {
  def apply(port: Int)(request: StubRequest,
    response: StubResponse,
    delay: Option[Int] = None,
    script: Option[String] = None) = {
    new StubExchangeOnPort(port, request, response, delay, script)
  }
}

/**
 * A StubExchange that is to occur on the
 * given port of localhost
 */
class StubExchangeOnPort(
    val port: Int,
    override val request: StubRequest,
    override val response: StubResponse,
    override val delay: Option[Int] = None,
    override val script: Option[String] = None) extends StubExchange(request, response, delay, script) {

}
