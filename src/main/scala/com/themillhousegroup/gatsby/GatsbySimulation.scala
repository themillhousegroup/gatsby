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
  def addExchange(requestName: String, se: StubExchange)
}

trait CanRemoveStubExchanges {
  def removeExchange(prefix: String)
}

trait DynamicStubExchange extends CanAddStubExchanges with CanRemoveStubExchanges

object GatsbyImplicits extends GatsbyImplicitsTrait {
}

trait GatsbyImplicitsTrait {
  implicit def s2eps(s: String) = ExpressionAndPlainString(stringToExpression(s), s)

}

/**
 * A Gatling Simulation that automagically spins up
 * "the thing at the back" so that we can actually test
 * "the thing in the middle"
 *
 */
abstract class AbstractGatsbySimulation(listenPort: Int) extends Simulation
    with HasStubbyServer
    with DynamicStubExchange
    with GatsbyAssertionSupport
    with GatsbyImplicitsTrait {

  private val logger = LoggerFactory.getLogger(getClass)

  val stubbyServer: StubbyServer

  implicit val simulation: AbstractGatsbySimulation = this

  /**
   * Any stub exchanges defined at this level will be added to the back end
   * and will exist for *all* scenarios.
   */
  val simulationWideExchanges: Seq[StubExchange]

  val scenarioExchanges = mutable.Map[String, StubExchange]()

  def addExchange(requestName: String, se: StubExchange) = {
    scenarioExchanges += (requestName -> se)
    logger.info(s"Adding scenario ($requestName) exchange: ${se.request.method.get} ${se.request.path.get}")
    stubbyServer.addExchange(se)
  }

  def removeExchange(prefix: String) = {
    logger.info(s"Removing scenario exchange for prefix: $prefix")
    scenarioExchanges.filter {
      case (k, _) => k.startsWith(prefix)
    }.foreach {
      case (k, v) => {
        logger.info(s"Removing scenario exchange for $k")
        stubbyServer.removeExchange(v)
        scenarioExchanges -= k
      }
    }

  }

  before {

    logger.info(s"Launching tame Stubby on port $listenPort")
    stubbyServer.start(listenPort)

    simulationWideExchanges.foreach { se =>
      logger.info("Adding stub exchange: " + se.request.method.get + " " + se.request.path.get)
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
