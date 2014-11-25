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
    with HasExtraStubbyServers
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

  val scenarioExchanges = mutable.Map[String, Seq[StubExchange]]()

  def addExchanges(requestName: String, ses: Seq[StubExchange]): Boolean = {
    val added = !scenarioExchanges.keySet.contains(requestName)

    // FIXME this does not really "add", it *replaces* all entries for the key.
    scenarioExchanges += (requestName -> ses)
    ses.foreach { se =>
      logger.info(s"Adding scenario ($requestName) exchange: ${se.request.method.get} ${se.request.path.get}")
      stubbyServer.addExchange(se)
    }
    added
  }

  def removeExchange(prefix: String): Boolean = {
    logger.info(s"Removing scenario exchange for prefix: $prefix")

    val initialLength = scenarioExchanges.size

    scenarioExchanges.filter {
      case (k, _) => k.startsWith(prefix)
    }.foreach {
      case (k, v) => {
        logger.info(s"Removing scenario exchanges (${v.length}) for $k")
        v.foreach(stubbyServer.removeExchange)
        scenarioExchanges -= k
      }
    }

    scenarioExchanges.size < initialLength
  }

  before {

    logger.info(s"Launching tame Stubby on port $listenPort")
    stubbyServer.start(listenPort)

    simulationWideExchanges.foreach { se =>
      if (se.isInstanceOf[StubExchangeOnPort]) {

        val seop = se.asInstanceOf[StubExchangeOnPort]
        val serverOnPort = stubbyServers.applyOrElse(seop.port, { port: Int =>
          val ts = new TameStubby()
          ts.start(port)
          stubbyServers += (port -> ts)
          ts
        })
        serverOnPort.addExchange(seop)
      } else {
        logger.info("Adding stub exchange: " + se.request.method.get + " " + se.request.path.get)
        stubbyServer.addExchange(se)
      }
    }
  }

  after {
    logger.info(s"Shutting down tame Stubby on port $listenPort")
    stubbyServer.stop

    stubbyServers.foreach {
      case (p, ss) =>
        logger.info(s"Shutting down tame Stubby on port $p")
        ss.stop
    }
  }
}

class GatsbySimulation(listenPort: Int) extends AbstractGatsbySimulation(listenPort) {
  val stubbyServer = new TameStubby()

  val stubbyServers = mutable.Map[Int, StubbyServer]()

  val simulationWideExchanges: Seq[StubExchange] = Nil
}
