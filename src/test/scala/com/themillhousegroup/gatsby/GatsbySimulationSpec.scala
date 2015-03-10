package com.themillhousegroup.gatsby

import org.specs2.mutable.Specification
import com.dividezero.stubby.core.model.{ StubRequest, StubExchange }
import org.specs2.mock.Mockito
import com.themillhousegroup.gatsby.stubby.StubbyServer
import com.themillhousegroup.gatsby.test.MockedLogging

class GatsbySimulationSpec extends Specification with Mockito {

  class TestGatsbySimulation(swes: Seq[StubExchange]) extends GatsbySimulation(8888) with MockedLogging {

    lazy val mockStubbyServer = mock[StubbyServer]
    override lazy val simulationWideExchanges = swes
    stubbyServers += (8888 -> mockStubbyServer)

    override def startStubbyOnPort(port: Int): StubbyServer = {
      mockStubbyServer
    }

    override def mainServer = mockStubbyServer
  }

  "GatsbySimulation" should {
    "Allow Simulation-wide exchanges to be defined" in {
      val mockExchange = mock[StubExchange]
      val testGatsbySimulation = new TestGatsbySimulation(Seq(mockExchange))

      testGatsbySimulation.before()

      testGatsbySimulation.simulationWideExchanges must haveLength(1)
      //      there was one(mockStubbyServer).addExchange(mockExchange)

    }

    "Allow single exchanges to be added per-request" in {
      val testGatsbySimulation = new TestGatsbySimulation(Nil)

      val mockStubExchange = givenStubExchange("GET", "/bar")

      testGatsbySimulation.addExchange("foo", mockStubExchange) must beTrue

      testGatsbySimulation.addExchange("foo", mockStubExchange) must beFalse
    }

    "Allow multiple exchanges to be added per-request" in {
      val testGatsbySimulation = new TestGatsbySimulation(Nil)

      val mockStubExchange1 = givenStubExchange("GET", "/bar")
      val mockStubExchange2 = givenStubExchange("POST", "/baz")

      testGatsbySimulation.addExchanges("foo", Seq(mockStubExchange1, mockStubExchange2)) must beTrue

      testGatsbySimulation.addExchange("foo", mockStubExchange1) must beFalse // We already know it

      there were two(testGatsbySimulation.mockStubbyServer).addExchange(any[StubExchange])
    }

    "Allow exchanges to be removed per-prefix" in {
      val testGatsbySimulation = new TestGatsbySimulation(Nil)

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
