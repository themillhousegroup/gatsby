package gatsbyexamples

import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.GatsbyHttpRequestActionBuilder.withStubby
import com.themillhousegroup.gatsby.GatsbySimulation
import com.themillhousegroup.gatsby.GatsbyHttp._
import io.gatling.http.Predef._

/**
 * This example shows how/proves that behaviour of endpoints can be stubbed on a
 * per-scenario basis, and that they won't interfere with one another.
 *
 */
class IndependentScenarioSimulation extends GatsbySimulation(9999) {

  val httpConf = http
    .baseURL("http://localhost:9999")

  val scn1 = scenario("IndependentScenarioSimulation1")
    .exec(
      gatsbyHttp("request_1")
        .get("/first", 200)
        .check(status.is(200))
    )
    .pause(1)


  val scn2 = scenario("IndependentScenarioSimulation2")
    .exec(
      withStubby(
      gatsbyHttp("request_2")
        .get("/first", 404)
        .check(status.is(404)))
    )
    .pause(1)

  setUp(
    scn1.inject(atOnceUsers(1)),
    scn2.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
