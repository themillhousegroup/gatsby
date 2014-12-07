package com.themillhousegroup.gatsby.actors

import com.dividezero.stubby.core.model.StubExchange
import akka.actor.ActorRef
import io.gatling.core.action.Chainable
import io.gatling.core.session.{ Expression, Session }
import com.themillhousegroup.gatsby.stubby.RuntimeStubbing
import com.typesafe.scalalogging.slf4j.{ StrictLogging, Logger }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise }

class SpinUp(val simulation: RuntimeStubbing,
    val requestNameExp: Expression[String],
    val ses: Seq[Expression[StubExchange]],
    val next: ActorRef) extends Chainable with CanSpinUp {
}

trait CanSpinUp {
  this: StrictLogging =>
  val simulation: RuntimeStubbing
  val requestNameExp: Expression[String]
  val ses: Seq[Expression[StubExchange]]
  val next: ActorRef
  private[this] val executionPromise = Promise[Boolean]

  // For testing; a marker that shows that the work has been done
  val executionComplete: Future[Boolean] = executionPromise.future

  def execute(session: Session): Unit = {

    requestNameExp(session).foreach { requestName =>

      logger.debug(s"Spinning up auto-response to $requestName for scenario: ${session.scenarioName}")
      simulation.acquireLock(requestName).map { ready =>
        ses.foreach(se => simulation.addExchange(requestName, se(session).get))
        next ! session
        executionPromise.success(ready)
      }
    }
  }
}
