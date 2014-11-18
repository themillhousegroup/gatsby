package com.themillhousegroup.gatsby.actors

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import io.gatling.core.session.{ Session, Expression }
import com.themillhousegroup.gatsby.stubby.RuntimeStubbing
import com.dividezero.stubby.core.model.StubExchange
import akka.actor.{ ActorSystem, ActorRef }
import scala.concurrent.{ Await, Future }
import io.gatling.core.validation.Success
import com.typesafe.scalalogging.slf4j.StrictLogging
import akka.testkit.TestActorRef
import com.themillhousegroup.gatsby.test.NextActor
import scala.concurrent.duration.Duration

class TearDownSpec extends Specification with Mockito {

  implicit val system = ActorSystem.create("TearDownSpec")
  val waitTime = Duration(5, "seconds")

  class TestTearDown(val simulation: RuntimeStubbing,
    val requestNameExp: Expression[String],
    val se: Expression[StubExchange],
    val next: ActorRef) extends CanTearDown with StrictLogging

  def tearDownWith(sim: RuntimeStubbing,
    next: ActorRef = TestActorRef[NextActor],
    requestName: String = "request",
    se: StubExchange = mock[StubExchange]) = {

    val mockReq = mock[Expression[String]]
    mockReq(any[Session]) returns Success(requestName)

    val mockSE = mock[Expression[StubExchange]]

    mockSE(any[Session]) returns Success(se)

    new TestTearDown(sim, mockReq, mockSE, next)
  }

  "TearDown Actor" should {

    "Remove the exchange for the request" in {
      val sim = mock[RuntimeStubbing]
      sim.acquireLock(anyString) returns Future.successful(true)
      val su = tearDownWith(sim)

      val session = mock[Session]
      session.scenarioName returns "scenarioName"
      su.execute(session)

      there was one(sim).removeExchange(anyString)

    }

    "Release the simulation lock" in {
      val sim = mock[RuntimeStubbing]
      sim.acquireLock(anyString) returns Future.successful(true)
      val su = tearDownWith(sim)

      val session = mock[Session]
      session.scenarioName returns "scenarioName"
      su.execute(session)

      there was one(sim).releaseLock(anyString)

    }

    "Call the next actor in the chain" in {

      val sim = mock[RuntimeStubbing]
      sim.acquireLock(anyString) returns Future.successful(true)

      val next = TestActorRef[NextActor]
      val su = tearDownWith(sim, next)

      val session = mock[Session]
      session.scenarioName returns "scenarioName"
      su.execute(session)

      val result = Await.result(next.underlyingActor.notified, waitTime)

      system.shutdown

      result must beEqualTo(session)
    }
  }
}
