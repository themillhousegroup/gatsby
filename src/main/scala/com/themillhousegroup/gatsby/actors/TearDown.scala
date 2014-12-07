package com.themillhousegroup.gatsby.actors

import akka.actor.ActorRef
import io.gatling.core.action.Chainable
import io.gatling.core.session.{ Expression, Session }
import com.themillhousegroup.gatsby.stubby.RuntimeStubbing
import com.dividezero.stubby.core.model.StubExchange
import com.typesafe.scalalogging.slf4j.{ StrictLogging, Logging, Logger }

class TearDown(val simulation: RuntimeStubbing,
    val requestNameExp: Expression[String],
    val next: ActorRef) extends Chainable with CanTearDown {

}

trait CanTearDown {
  this: StrictLogging =>
  val simulation: RuntimeStubbing
  val requestNameExp: Expression[String]
  val next: ActorRef

  def execute(session: Session): Unit = {
    requestNameExp(session).foreach { requestName =>
      logger.debug(s"Tearing down $requestName after scenario: ${session.scenarioName}")
      simulation.removeExchange(requestName)
      simulation.releaseLock(requestName)
      next ! session
    }
  }

}
