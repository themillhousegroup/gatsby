package gatsbyexamples

import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.GatsbySimulation
import com.dividezero.stubby.core.model.{StubResponse, StubRequest, StubExchange}

// 2
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicGatsbySimulation extends GatsbySimulation(9999) {

  val stubExchanges = Seq(
    StubExchange( StubRequest(Some("GET"), Some("/"), Nil, Nil, None),
                  StubResponse(200, Nil, None))
  )

  val httpConf = http
    .baseURL("http://localhost:9999")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val scn = scenario("BasicGatsbySimulation")
    .exec(http("request_1")
      .get("/"))
    .pause(5)

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
