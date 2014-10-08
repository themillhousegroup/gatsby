package computerdatabase // 1

import io.gatling.core.Predef._
import com.themillhousegroup.gatsby.GatsbySimulation
import com.dividezero.stubby.core.model.{StubResponse, StubRequest, StubExchange}

// 2
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicGatsbySimulation extends GatsbySimulation(9999) { // 3

  val stubExchanges = Seq(
    StubExchange( StubRequest(Some("GET"), Some("/"), Nil, Nil, None),
                  StubResponse(203, Nil, None))
  )

  val httpConf = http // 4
    .baseURL("http://localhost:9999") // 5
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val scn = scenario("BasicGatsbySimulation") // 7
    .exec(http("request_1")  // 8
      .get("/")) // 9
    .pause(5) // 10

  setUp( // 11
    scn.inject(atOnceUsers(1)) // 12
  ).protocols(httpConf) // 13
}
