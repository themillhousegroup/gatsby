package com.themillhousegroup.gatsby

import io.gatling.core.scenario.Simulation
import com.dividezero.stubby.core.model.StubExchange
import org.slf4j.LoggerFactory
import com.dividezero.stubby.core.model.StubExchange
import scala.collection.mutable
import io.gatling.core.session._
import com.dividezero.stubby.core.model.StubExchange
import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.ExpressionAndPlainString
import com.dividezero.stubby.core.model.StubExchange

case class ExpressionAndPlainString(exp: Expression[String], plain: String)

/**
 * A Gatling Simulation that automagically spins up
 * "the thing at the back" so that we can actually test
 * "the thing in the middle"
 *
 */
abstract class GatsbySimulation(listenPort: Int) extends Simulation {

  private val logger = LoggerFactory.getLogger(getClass)

  implicit def s2eps(s: String) = ExpressionAndPlainString(stringToExpression(s), s)

  val stubbyServer = new TameStubby()

  implicit val simulation: GatsbySimulation = this

  /** Any stub exchanges defined at this level will be added to the back end */
  val stubExchanges: Seq[StubExchange]

  val scenarioExchanges = mutable.Buffer[StubExchange]()

  def addExchange(se: StubExchange) = {
    scenarioExchanges += se
  }

  before {

    logger.info(s"Launching tame Stubby on port $listenPort")
    stubbyServer.start(listenPort)

    stubExchanges.foreach { se =>
      logger.info("Adding stub exchange: " + se.request.method.get + " " + se.request.path.get)
      stubbyServer.addExchange(se)
    }

    scenarioExchanges.foreach { se =>
      logger.info("Adding scenario exchange: " + se.request.method.get + " " + se.request.path.get)
      stubbyServer.addExchange(se)
    }

  }

  after {
    logger.info(s"Shutting down tame Stubby on port $listenPort")
    stubbyServer.stop
  }
}
