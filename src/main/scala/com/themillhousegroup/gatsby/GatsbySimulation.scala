package com.themillhousegroup.gatsby

import scala.collection.mutable
import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.assertions.GatsbyAssertionSupport
import com.themillhousegroup.gatsby.stubby._
import com.dividezero.stubby.core.model.StubExchange

/**
 * A Gatling Simulation that automagically spins up
 * "the thing at the back" so that we can actually test
 * "the thing in the middle"
 *
 */
abstract class AbstractGatsbySimulation(listenPort: Int) extends Simulation
    with HasStubbyServer
    with RuntimeStubbing
    with GatsbyAssertionSupport
    with HasLogger {

  val stubbyServer: StubbyServer

  implicit val simulation: AbstractGatsbySimulation = this

  /**
   * Any stub exchanges defined at this level will be added to the back end
   * and will exist for *all* scenarios.
   */
  val simulationWideExchanges: Seq[StubExchange]

  val scenarioExchanges = mutable.Map[String, StubExchange]()

  def addExchange(requestName: String, se: StubExchange): Boolean = {
    val added = !scenarioExchanges.keySet.contains(requestName)
    scenarioExchanges += (requestName -> se)
    logger.info(s"Adding scenario ($requestName) exchange: ${se.request.method.get} ${se.request.path.get}")
    stubbyServer.addExchange(se)
    added
  }

  def removeExchange(prefix: String): Boolean = {
    logger.info(s"Removing scenario exchange for prefix: $prefix")

    val initialLength = scenarioExchanges.size

    scenarioExchanges.filter {
      case (k, _) => k.startsWith(prefix)
    }.foreach {
      case (k, v) => {
        logger.info(s"Removing scenario exchange for $k")
        stubbyServer.removeExchange(v)
        scenarioExchanges -= k
      }
    }

    scenarioExchanges.size < initialLength
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
