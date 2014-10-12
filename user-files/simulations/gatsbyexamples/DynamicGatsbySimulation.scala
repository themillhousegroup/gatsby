package gatsbyexamples

import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.GatsbySimulation
import com.themillhousegroup.gatsby.GatsbyHttp._
import io.gatling.http.Predef._


class DynamicGatsbySimulation extends GatsbySimulation(9999) {

  val httpConf = http
    .baseURL("http://localhost:9999")

  val scn1 = scenario("DynamicGatsbySimulation1")
    .exec(
      gatsbyHttp("request_1")(this)
      .get("/first")
      .check(status))
    .pause(5)


  val scn2 = scenario("DynamicGatsbySimulation2")
    .exec(gatsbyHttp("request_2")(this)
    .get("/second"))
    .pause(5)

  setUp(
    scn1.inject(atOnceUsers(1)),
    scn2.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
