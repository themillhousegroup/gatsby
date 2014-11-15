package com.themillhousegroup.gatsby.actors

import com.dividezero.stubby.core.model.StubExchange
import akka.actor.ActorRef
import io.gatling.core.action.Chainable
import io.gatling.core.session.{ Expression, Session }
import com.themillhousegroup.gatsby.stubby.RuntimeStubbing
import com.typesafe.scalalogging.slf4j.Logger
import scala.concurrent.ExecutionContext.Implicits.global

class SpinUp(val simulation: RuntimeStubbing,
    val requestNameExp: Expression[String],
    val se: Expression[StubExchange],
    val next: ActorRef) extends Chainable with CanSpinUp {

}

trait CanSpinUp {
  val simulation: RuntimeStubbing
  val requestNameExp: Expression[String]
  val se: Expression[StubExchange]
  val next: ActorRef
  protected val logger: Logger

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
