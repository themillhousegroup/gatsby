package gatsbyexamples

import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.GatsbySimulation
import com.themillhousegroup.gatsby.GatsbyHttp.gatsbyHttp
import com.dividezero.stubby.core.model.{StubResponse, StubRequest, StubExchange}
import io.gatling.http.Predef._

/**
 * For this to pass, you'll need some device/tool configured as follows:
 *
 * GATLING => localhost:8888 => <THING> => localhost:9999 => STUBBY
 *
 * There are two scenarios here.
 * The positive one checks that <THING> is doing port-forwarding for /public
 * The negative one asserts that access to /secret gets blocked.
 */
class FilteringSimulation extends GatsbySimulation(9999) {

  val httpConf = http.baseURL("http://localhost:8888")

  val scn1 = scenario("AllowedPage")
    .exec(gatsbyHttp("allowed-req-1").get("/public"))
    .pause(1)

  val scn2 = scenario("BlockedPage")
    .exec(gatsbyHttp("blocked-req-1")
    .get("/secret")
    .check(status.is(403))
  )
    .pause(1)

  setUp(
    scn1.inject(atOnceUsers(1)),
    scn2.inject(atOnceUsers(1))
  )
    .protocols(httpConf)
    .assertions(
      global.successfulRequests.count.is(2),
      stubby.requestsSeen.is(1),
      stubby.requestsSeenFor("/secret").is(0))

}


