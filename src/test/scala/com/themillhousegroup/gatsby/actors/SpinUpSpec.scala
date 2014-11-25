package com.themillhousegroup.gatsby.actors

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import io.gatling.core.session.{ Session, Expression }
import com.themillhousegroup.gatsby.stubby.RuntimeStubbing
import com.dividezero.stubby.core.model.StubExchange
import akka.actor.ActorRef
import scala.concurrent.{ Await, Promise, Future }
import io.gatling.core.validation.Success
import com.typesafe.scalalogging.slf4j.StrictLogging
import akka.testkit.TestActorRef
import scala.concurrent.duration.Duration
import com.themillhousegroup.gatsby.test.{ ActorScope, NextActor }

class SpinUpSpec extends Specification with Mockito {

  val waitTime = Duration(5, "seconds")

  class TestSpinUp(val simulation: RuntimeStubbing,
    val requestNameExp: Expression[String],
    val ses: Seq[Expression[StubExchange]],
    val next: ActorRef) extends CanSpinUp with StrictLogging

  def spinUpWith(sim: RuntimeStubbing,
    next: ActorRef,
    requestName: String = "request",
    se: StubExchange = mock[StubExchange]) = {

    val mockReq = mock[Expression[String]]
    mockReq(any[Session]) returns Success(requestName)

    val mockSE = mock[Expression[StubExchange]]

    mockSE(any[Session]) returns Success(se)

    new TestSpinUp(sim, mockReq, Seq(mockSE), next)
  }

  "SpinUp Actor" should {

    "Execute immediately if there is no contention for the simulation lock" in new ActorScope {
      val sim = mock[RuntimeStubbing]
      sim.acquireLock(anyString) answers (_ => Future.successful(true))
      val su = spinUpWith(sim, TestActorRef[NextActor])

      val session = mock[Session]
      session.scenarioName returns "scenarioName"
      su.execute(session)

      there was one(sim).addExchange(anyString, any[StubExchange])

    }

    "Call the next actor in the chain" in new ActorScope {

      val sim = mock[RuntimeStubbing]
      sim.acquireLock(anyString) returns Future.successful(true)

      val next = TestActorRef[NextActor]
      val su = spinUpWith(sim, next)

      val session = mock[Session]
      session.scenarioName returns "scenarioName"
      su.execute(session)

      val result = Await.result(next.underlyingActor.notified, waitTime)

      system.shutdown

      result must beEqualTo(session)
    }

    "Await the simulation lock before adding an exchange" in new ActorScope {
      val sim = mock[RuntimeStubbing]
      sim.acquireLock(anyString) answers { _ =>
        Future.successful {
          Thread.sleep(1500)
          true
        }
      }
      val su = spinUpWith(sim, TestActorRef[NextActor])

      val session = mock[Session]
      session.scenarioName returns "scenarioName"
      Future { su.execute(session) }

      there was no(sim).addExchange(anyString, any[StubExchange])

      Thread.sleep(3000)

      there was one(sim).addExchange(anyString, any[StubExchange])
    }
  }
}

