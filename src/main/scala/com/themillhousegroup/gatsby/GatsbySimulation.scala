package com.themillhousegroup.gatsby

import io.gatling.core.scenario.Simulation
import com.dividezero.stubby.core.model.StubExchange
import org.slf4j.LoggerFactory

/**
 * A Gatling {@link Simulation} that automagically spins up
 * "the thing at the back" so that we can actually test
 * "the thing in the middle"
 *
 */
abstract class GatsbySimulation(listenPort: Int) extends Simulation {

  private val logger = LoggerFactory.getLogger(getClass)

  val stubbyServer = new TameStubby()

  /** Any stub exchanges defined at this level will be added to the back end */
  val stubExchanges: Seq[StubExchange]

  before {

    logger.info(s"Launching tame Stubby on port $listenPort")
    stubbyServer.start(listenPort)

    stubExchanges.foreach { se =>
      logger.info("Adding stub exchange: " + se.request.method.get + " " + se.request.path.get)
      stubbyServer.addExchange(se)
    }
  }

  after {
    logger.info(s"Shutting down tame Stubby on port $listenPort")
    stubbyServer.stop
  }
}
