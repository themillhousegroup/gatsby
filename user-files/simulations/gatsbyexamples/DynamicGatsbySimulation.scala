package gatsbyexamples

import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.GatsbySimulation
import com.themillhousegroup.gatsby.GatsbyHttpActionBuilder._
import io.gatling.http.Predef._


/**
 * Showing how simply wrapping a call with withStubby() will
 * cause an endpoint that returns 200 OK to be stubbed in.
 */
class DynamicGatsbySimulation extends GatsbySimulation(9999) {

  val httpConf = http.baseURL("http://localhost:9999")

  val scn1 = scenario("DynamicGatsbySimulation1")
    .exec(
      withStubby(
        http("request_1").get("/first").check(status.is(200))
      )
    )


  val scn2 = scenario("DynamicGatsbySimulation2")
    .exec(
      withStubby(
        http("request_2").get("/second").check(status.is(200)))
    )

  val scn3 = scenario("DynamicGatsbySimulation3")
    .exec(
      withStubby(
        http("request_3").post("/postUrl").check(status.is(200)))
  )

  setUp(
    scn1.inject(atOnceUsers(3)),
    scn2.inject(atOnceUsers(3)),
    scn3.inject(atOnceUsers(3))
  ).protocols(httpConf)
}
