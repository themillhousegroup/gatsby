package com.themillhousegroup.gatsby

import org.specs2.mutable.Specification
import com.dividezero.stubby.core.model.{ StubRequest, StubExchange }
import org.specs2.mock.Mockito
import com.themillhousegroup.gatsby.stubby.StubbyServer

class GatsbySimulationSpec extends Specification with Mockito {

  val mockStubbyServer = mock[StubbyServer]

  class TestGatsbySimulation(val simulationWideExchanges: Seq[StubExchange]) extends AbstractGatsbySimulation(8888) {
    val stubbyServer = mockStubbyServer
    val stubbyServers = scala.collection.mutable.Map[Int, StubbyServer]()
  }

  val mockExchange = mock[StubExchange]

  "GatsbySimulation" should {
    "Allow Simulation-wide exchanges to be defined" in {
      val testGatsbySimulation = new TestGatsbySimulation(Seq(mockExchange))

      testGatsbySimulation.before()

      testGatsbySimulation.simulationWideExchanges must haveLength(1)
      //      there was one(mockStubbyServer).addExchange(mockExchange)

    }

    "Allow exchanges to be added per-request" in {
      val testGatsbySimulation = new TestGatsbySimulation(Seq(mockExchange))

      val mockStubExchange = givenStubExchange("GET", "/bar")

      testGatsbySimulation.addExchange("foo", mockStubExchange) must beTrue

      testGatsbySimulation.addExchange("foo", mockStubExchange) must beFalse
    }

    "Allow exchanges to be removed per-prefix" in {
      val testGatsbySimulation = new TestGatsbySimulation(Seq(mockExchange))

      val mockStubExchange = givenStubExchange("GET", "/bar")

      testGatsbySimulation.addExchange("foo", mockStubExchange) must beTrue

      testGatsbySimulation.removeExchange("fo") must beTrue

      testGatsbySimulation.removeExchange("fox") must beFalse

      testGatsbySimulation.removeExchange("fo") must beFalse
    }
  }

  def givenStubExchange(method: String, path: String) = {
    val mockStubExchange = mock[StubExchange]
    val mockStubRequest = mock[StubRequest]
    mockStubExchange.request returns mockStubRequest
    mockStubRequest.method returns Some(method)
    mockStubRequest.path returns Some(path)
    mockStubExchange
  }
}
