package com.themillhousegroup.gatsby

import scala.collection.mutable
import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.assertions.GatsbyAssertionSupport
import com.themillhousegroup.gatsby.stubby._
import com.dividezero.stubby.core.model.StubExchange
import com.typesafe.scalalogging.slf4j.Logging

/**
 * A Gatling Simulation that automagically spins up
 * "the thing at the back" so that we can actually test
 * "the thing in the middle"
 *
 */
class GatsbySimulation(listenPort: Int) extends Simulation
    with RuntimeStubbing
    with GatsbyAssertionSupport
    with Logging {

  val stubbyServers = mutable.Map[Int, StubbyServer]()

  implicit val simulation: GatsbySimulation = this

  /**
   * Any stub exchanges defined at this level will be added to the back end
   * and will exist for *all* scenarios.
   */
  lazy val simulationWideExchanges: Seq[StubExchange] = Nil

  val scenarioExchanges = mutable.Map[String, mutable.Buffer[StubExchange]]()

  def addExchanges(requestName: String, ses: Seq[StubExchange]): Boolean = {

    val exchanges = scenarioExchanges.applyOrElse(requestName, { key: String =>
      val tuple = key -> mutable.Buffer[StubExchange]()
      scenarioExchanges += tuple
      tuple._2
    })

    val diff = ses.diff(exchanges)

    diff.foreach { se =>
      logger.info(s"Adding scenario ($requestName) exchange: ${se.request.method.get} ${se.request.path.get}")
      mainServer.addExchange(se)
      exchanges += se
    }

    diff.nonEmpty
  }

  def removeExchange(prefix: String): Boolean = {
    logger.info(s"Removing scenario exchange for prefix: $prefix")

    val initialLength = scenarioExchanges.size

    scenarioExchanges.filter {
      case (k, _) => k.startsWith(prefix)
    }.foreach {
      case (k, v) => {
        logger.info(s"Removing scenario exchanges (${v.length}) for $k")
        v.foreach(mainServer.removeExchange)
        scenarioExchanges -= k
      }
    }

    scenarioExchanges.size < initialLength
  }

  def startStubbyOnPort(port: Int): StubbyServer = {
    val ts = new TameStubby()
    ts.start(port)
    stubbyServers += (port -> ts)
    ts
  }

  def mainServer = stubbyServers(listenPort)

  logger.info(s"Launching tame Stubby on port $listenPort")
  startStubbyOnPort(listenPort)

  simulationWideExchanges.foreach { se =>
    if (se.isInstanceOf[StubExchangeOnPort]) {

      val seop = se.asInstanceOf[StubExchangeOnPort]
      val serverOnPort = stubbyServers.applyOrElse(seop.port, startStubbyOnPort)
      serverOnPort.addExchange(seop)
    } else {
      logger.info("Adding stub exchange: " + se.request.method.get + " " + se.request.path.get)
      mainServer.addExchange(se)
    }
  }

  after {
    stubbyServers.foreach {
      case (p, ss) =>
        logger.info(s"Shutting down tame Stubby on port $p")
        ss.stop
    }
  }
}
