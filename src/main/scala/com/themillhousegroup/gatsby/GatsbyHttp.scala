package com.themillhousegroup.gatsby

import org.slf4j.LoggerFactory
import io.gatling.http.request.builder.{ HttpParam, HttpAttributes }
import com.dividezero.stubby.core.model.StubExchange
import io.gatling.http.check.HttpCheck
import io.gatling.http.check.HttpCheckScope.Status
import io.gatling.http.response.Response
import scala.collection.mutable
import io.gatling.core.validation.Validation
import io.gatling.core.check.CheckResult
import io.gatling.core.session._
import io.gatling.core.Predef._

object GatsbyHttp {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def gatsbyHttp(requestName: String)(implicit simulation: DynamicStubExchange) = {
    logger.info(s"Using Gatsby to generate responses for ${simulation.getClass.getSimpleName}")
    new GatsbyHttp(requestName, requestName, simulation)
  }

  val applicationJson = Some("application/json")
}

/**
 * By declaring a test endpoint with one of these "url:ExpressionAndPlainString" methods,
 * you're indicating that you'd like Gatsby to set
 * up Stubby endpoints for each one.
 */
class GatsbyHttp(requestName: String, requestNameExp: Expression[String], simulation: DynamicStubExchange) extends AbstractGatsbyHttp(requestName, requestNameExp, simulation) {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def get(url: ExpressionAndPlainString) = httpRequest("GET", url)
  def post(url: ExpressionAndPlainString) = httpRequestWithParams("POST", url)

  def post(url: ExpressionAndPlainString,
    httpAttributes: HttpAttributes,
    formParams: List[HttpParam]) = {

    httpRequestWithParams("POST", url, httpAttributes, formParams).check(new GatsbyPostScenarioCleanup)
  }

  def put(url: ExpressionAndPlainString) = httpRequest("PUT", url)
  def patch(url: ExpressionAndPlainString) = httpRequest("PATCH", url)
  def head(url: ExpressionAndPlainString) = httpRequest("HEAD", url)
  def delete(url: ExpressionAndPlainString) = httpRequest("DELETE", url)
  def options(url: ExpressionAndPlainString) = httpRequest("OPTIONS", url)

  /**
   * If you have many stub exchanges to set up, configure them here
   */
  def withAdditionalStubbing(exchanges: StubExchange*): GatsbyHttp = {
    exchanges.foreach { ex =>
      val uniqueName = s"${requestName}--${ex.request.method.get}--${ex.request.path.get}"
      simulation.addExchange(uniqueName)(ex)
    }
    this
  }

  /**
   * Shortcut method when you just need one quick-and-dirty stub endpoint that returns a status, and optionally a body and a given Content-Type
   */
  def withAdditionalStubbing(method: String, url: String, responseStatus: Int = 200, responseBody: Option[AnyRef] = None, responseContentType: Option[String] = None): GatsbyHttp = {
    withAdditionalStubbing(buildExchange(method, url, responseStatus, responseBody, responseContentType))
  }

  object GatsbyCheck extends io.gatling.core.check.Check[io.gatling.http.response.Response] {
    def check(response: Response, session: Session)(implicit cache: mutable.Map[Any, Any]): Validation[CheckResult] = {
      println("Gatsby post-scenario check RUNNING")
      simulation.removeExchange(requestName)
      CheckResult.NoopCheckResultSuccess
    }
  }

  class GatsbyPostScenarioCleanup extends HttpCheck(GatsbyCheck, Status, None) {

  }
}

