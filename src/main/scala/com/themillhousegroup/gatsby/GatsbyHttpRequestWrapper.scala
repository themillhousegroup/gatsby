package com.themillhousegroup.gatsby

import io.gatling.http.request.builder.{ HttpParam, HttpAttributes, CommonAttributes, HttpRequestWithParamsBuilder }
import io.gatling.http.config.HttpProtocol
import io.gatling.http.request.HttpRequestDef

class GatsbyHttpRequestWrapper {

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
