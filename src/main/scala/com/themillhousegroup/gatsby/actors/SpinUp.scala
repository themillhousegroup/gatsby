package com.themillhousegroup.gatsby.actors

import com.themillhousegroup.gatsby.{ HasLogger, DynamicStubExchange }
import com.dividezero.stubby.core.model.StubExchange
import akka.actor.ActorRef
import io.gatling.core.action.Chainable
import io.gatling.core.session.Session

class SpinUp(val simulation: DynamicStubExchange, val requestName: String, val se: StubExchange, val next: ActorRef) extends Chainable {

  def execute(session: Session): Unit = {
    logger.debug(s"Spinning up auto-response to $requestName for scenario: ${session.scenarioName}")
    simulation.acquireLock(requestName).map { ready =>
      simulation.addExchange(requestName, se)
      next ! session
    }
  }
}
