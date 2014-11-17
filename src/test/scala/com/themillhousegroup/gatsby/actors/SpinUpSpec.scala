package com.themillhousegroup.gatsby.actors

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import io.gatling.core.session.{ Session, Expression }
import com.themillhousegroup.gatsby.stubby.RuntimeStubbing
import com.dividezero.stubby.core.model.StubExchange
import akka.actor.{ Actor, ActorSystem, ScalaActorRef, ActorRef }
import scala.concurrent.{ Await, Promise, Future }
import io.gatling.core.validation.Success
import com.typesafe.scalalogging.slf4j.StrictLogging
import akka.testkit.{ TestProbe, TestActorRef }
import scala.concurrent.duration.Duration

class SpinUpSpec extends Specification with Mockito {

  val waitTime = Duration(5, "seconds")

  class TestSpinUp(val simulation: RuntimeStubbing,
    val requestNameExp: Expression[String],
    val se: Expression[StubExchange],
    val next: ActorRef) extends CanSpinUp with StrictLogging

  def spinUpWith(sim: RuntimeStubbing,
    next: ActorRef = mock[ActorRef],
    requestName: String = "request",
    se: StubExchange = mock[StubExchange]) = {

    val mockReq = mock[Expression[String]]
    mockReq(any[Session]) returns Success(requestName)

    val mockSE = mock[Expression[StubExchange]]

    mockSE(any[Session]) returns Success(se)

    new TestSpinUp(sim, mockReq, mockSE, next)
  }

  "SpinUp Actor" should {

    "Execute immediately if there is no contention for the simulation lock" in {
      val sim = mock[RuntimeStubbing]
      sim.acquireLock(anyString) returns Future.successful(true)
      val su = spinUpWith(sim)

      val session = mock[Session]
      session.scenarioName returns "scenarioName"
      Future { su.execute(session) }

      Thread.sleep(2000)

      there was one(sim).addExchange(anyString, any[StubExchange])

    }

    "Call the next actor in the chain" in {

      implicit val system = ActorSystem.create("SpinUpSpec")

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

    "Await the simulation lock before adding an exchange" in {
      val sim = mock[RuntimeStubbing]
      sim.acquireLock(anyString) answers { _ =>
        Future.successful {
          Thread.sleep(1500)
          true
        }
      }
      val su = spinUpWith(sim)

      val session = mock[Session]
      session.scenarioName returns "scenarioName"
      Future { su.execute(session) }

      there was no(sim).addExchange(anyString, any[StubExchange])

      Thread.sleep(3000)

      there was one(sim).addExchange(anyString, any[StubExchange])
    }
  }
}

class NextActor extends Actor {

  private[this] val notificationPromise = Promise[Session]
  val notified: Future[Session] = notificationPromise.future

  def receive: Actor.Receive = {
    case s: Session => notificationPromise.success(s)
    case x: Any => notificationPromise.failure(new IllegalArgumentException(s"Wanted a Session, got: $x"))
  }
}
