package com.themillhousegroup.gatsby.actors

import com.themillhousegroup.gatsby.{ HasLogger, DynamicStubExchange }
import com.dividezero.stubby.core.model.StubExchange
import akka.actor.ActorRef
import io.gatling.core.action.Chainable
import io.gatling.core.session.{ Expression, Session }

class SpinUp(val simulation: DynamicStubExchange, val requestNameExp: Expression[String], val se: Expression[StubExchange], val next: ActorRef) extends Chainable {

  def execute(session: Session): Unit = {

    requestNameExp(session).foreach { requestName =>

      logger.debug(s"Spinning up auto-response to $requestName for scenario: ${session.scenarioName}")
      simulation.acquireLock(requestName).map { ready =>
        simulation.addExchange(requestName, se(session).get)
        next ! session
      }
    }

  }
}
