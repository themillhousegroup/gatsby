package com.themillhousegroup.gatsby

import io.gatling.core.session._
import org.slf4j.LoggerFactory

object GatsbyHttp {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def gatsbyHttp(requestName: Expression[String])(simulation: GatsbySimulation) = {
    logger.info(s"Using Gatsby to generate responses for ${simulation.getClass.getSimpleName}")
    new GatsbyHttp(requestName, simulation)
  }
}

/**
 * By declaring a test endpoint with one of these "url:ExpressionAndPlainString" methods,
 * you're indicating that you'd like Gatsby to set
 * up Stubby endpoints for each one.
 */
class GatsbyHttp(requestName: Expression[String], simulation: GatsbySimulation) extends AbstractGatsbyHttp(requestName, simulation) {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  def get(url: ExpressionAndPlainString) = {
    logger.info(s"Gatsby GET of ${url.plain}")

    httpRequest("GET", url)
  }

}
