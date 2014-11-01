package com.themillhousegroup.gatsby.actors

import com.themillhousegroup.gatsby.{ HasLogger, DynamicStubExchange }
import akka.actor.ActorRef
import io.gatling.core.action.Chainable
import io.gatling.core.session.{ Expression, Session }

class TearDown(val simulation: DynamicStubExchange, val requestNameExp: Expression[String], val next: ActorRef) extends Chainable {

  def execute(session: Session): Unit = {
    requestNameExp(session).foreach { requestName =>
      logger.debug(s"Tearing down $requestName after scenario: ${session.scenarioName}")
      simulation.removeExchange(requestName)
      simulation.releaseLock(requestName)
      next ! session
    }
  }
}
