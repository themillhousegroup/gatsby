package gatsbyexamples

import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.GatsbyHttpActionBuilder._
import com.themillhousegroup.gatsby.GatsbySimulation
import io.gatling.http.Predef._

/**
 * This example shows how/proves that behaviour of ONE endpoint can be stubbed on a
 * per-scenario basis, and that they won't interfere with one another.
 *
 */
class IndependentScenarioSimulation extends GatsbySimulation(9999) {

  val httpConf = http.baseURL("http://localhost:9999")

  val scn1 = scenario("IndependentScenarioSimulation1")
    .exec(withStubby(http("request_should_be_ok").get("/first").check(status.is(200))))
    .pause(1)


  val scn2 = scenario("IndependentScenarioSimulation2")
    .exec(withStubby(404)(http("request_should_get_404").get("/first").check(status.is(404))))
    .pause(1)

  setUp(
    scn1.inject(atOnceUsers(1)),
    scn2.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
