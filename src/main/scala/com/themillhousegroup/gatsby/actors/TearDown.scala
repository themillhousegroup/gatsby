package com.themillhousegroup.gatsby.actors

import com.themillhousegroup.gatsby.{HasLogger, DynamicStubExchange}
import akka.actor.ActorRef
import io.gatling.core.action.Chainable
import io.gatling.core.session.Session

class TearDown(val simulation: DynamicStubExchange, val requestName: String, val next: ActorRef) extends Chainable with HasLogger{

  def execute(session: Session): Unit = {
    logger.debug(s"Tearing down $requestName after scenario: ${session.scenarioName}")
    simulation.removeExchange(requestName)
    simulation.releaseLock(requestName)
    next ! session
  }
}
