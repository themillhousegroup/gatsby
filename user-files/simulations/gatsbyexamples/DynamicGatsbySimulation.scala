package gatsbyexamples

import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.GatsbySimulation
import com.themillhousegroup.gatsby.GatsbyHttp._
import io.gatling.http.Predef._


class DynamicGatsbySimulation extends GatsbySimulation(9999) {

  val stubExchanges = Nil

  val httpConf = http
    .baseURL("http://localhost:9999")

  val scn = scenario("DynamicGatsbySimulation")
    .exec(gatsbyHttp("request_1")(this)
      .get("/foo/bar"))
    .pause(5)

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
