package com.themillhousegroup.gatsby

import org.specs2.mutable.Specification
import com.dividezero.stubby.core.model.StubExchange
import org.specs2.mock.Mockito

class GatsbySimulationSpec extends Specification with Mockito {

  val mockStubbyServer = mock[StubbyServer]

  class TestGatsbySimulation(val stubExchanges: Seq[StubExchange]) extends AbstractGatsbySimulation(8888) {
    val stubbyServer = mockStubbyServer
  }

  val mockExchange = mock[StubExchange]

  "GatsbySimulation" should {

    "Allow Simulation-wide exchanges to be defined" in {
      val testGatsbySimulation = new TestGatsbySimulation(Seq(mockExchange))

      testGatsbySimulation.before()

      testGatsbySimulation.stubExchanges must haveLength(1)
      //      there was one(mockStubbyServer).addExchange(mockExchange)

    }
  }
}
