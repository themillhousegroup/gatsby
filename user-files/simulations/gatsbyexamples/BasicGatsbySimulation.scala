package gatsbyexamples

import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.GatsbySimulation
import com.dividezero.stubby.core.model.{StubRequest, StubResponse, StubExchange}
import io.gatling.http.Predef._
import com.themillhousegroup.gatsby.stubby.StubExchangeOnPort
import com.dividezero.stubby.core.model.StubRequest

/**
 * If all you need is an unchanging backend for your tests,
 * define some 'simulationWideExchanges'.
 *
 * You can also make some simple assertions about that backend at the end of the simulation.
 */
class BasicGatsbySimulation extends GatsbySimulation(9999) {

  override lazy val simulationWideExchanges = Seq(
    StubExchange( StubRequest(Some("GET"), Some("/"), Nil, Nil, None),
                  StubResponse(200, Nil, None)),
    StubExchangeOnPort (8888)(StubRequest(Some("GET"), Some("/"), Nil, Nil, None),
                              StubResponse(200, Nil, None))
  )

  val httpConf = http
    .baseURL("http://localhost:9999")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val scn = scenario("BasicGatsbySimulation")
    .exec(http("request_1")
      .get("/"))
    .pause(1)

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)
    .assertions(
    global.successfulRequests.count.is(1),
    stubby.requestsSeen.is(1),
    stubby.requestsSeenFor("/").is(1),
    stubby.requestsSeenFor("/blurg").is(0)
  )
}
