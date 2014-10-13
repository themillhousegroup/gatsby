package com.themillhousegroup.gatsby

import org.slf4j.LoggerFactory
import scala.collection.mutable
import io.gatling.core.session._
import io.gatling.core.Predef._
import com.dividezero.stubby.core.model.StubExchange

case class ExpressionAndPlainString(exp: Expression[String], plain: String)

trait HasStubbyServer {
  val stubbyServer: StubbyServer
}

trait CanAddStubExchanges {
  def addExchange(se: StubExchange)
}

/**
 * A Gatling Simulation that automagically spins up
 * "the thing at the back" so that we can actually test
 * "the thing in the middle"
 *
 */
abstract class AbstractGatsbySimulation(listenPort: Int) extends Simulation
    with HasStubbyServer
    with CanAddStubExchanges
    with GatsbyAssertionSupport {

  private val logger = LoggerFactory.getLogger(getClass)

  implicit def s2eps(s: String) = ExpressionAndPlainString(stringToExpression(s), s)

  val stubbyServer: StubbyServer

  implicit val simulation: AbstractGatsbySimulation = this

  /**
   * Any stub exchanges defined at this level will be added to the back end
   * and will exist for *all* scenarios.
   */
  val simulationWideExchanges: Seq[StubExchange]

  val scenarioExchanges = mutable.Buffer[StubExchange]()

  def addExchange(se: StubExchange) = {
    scenarioExchanges += se
  }

  before {

    logger.info(s"Launching tame Stubby on port $listenPort")
    stubbyServer.start(listenPort)

    simulationWideExchanges.foreach { se =>
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

class GatsbySimulation(listenPort: Int) extends AbstractGatsbySimulation(listenPort) {
  val stubbyServer = new TameStubby()

  val simulationWideExchanges: Seq[StubExchange] = Nil
}
