gatsby
============================

__Gat__[ling] at the front, __s__[tub]__by__ round the back.

Put your network tool/middleware/whatever in the middle and verify it does what it should.

### Usage
Check out [Gatling's excellent documentation](http://gatling.io/docs/2.0.0/) to understand how to record and/or write files in the Gatling DSL. Once you're comfortable with that, just use some of the additional Gatsby classes to get the auto-Stubby working.

#### Examples
Check out the files in [user-files/simulations/gatsbyexamples](https://github.com/themillhousegroup/gatsby/tree/master/user-files/simulations/gatsbyexamples), but here are some highlights:

##### A backend endpoint that's always there throughout the Simulation
In this example, we stub an endpoint on `localhost:9999` to respond to `GET /health.html` that is always `200 OK`:

```
class HealthySimulation extends GatsbySimulation(9999) {

  override val simulationWideExchanges = Seq(
    StubExchange( StubRequest(Some("GET"), Some("/health.html"), Nil, Nil, None),
                  StubResponse(200, Nil, None))
  )

  // Normal Gatling DSL scenarios etc here.
  // ...
  
}
```

##### Assert that things are getting filtered out
In this example, we have a device under test listening on `localhost:8888`, which we expect to  forward (to our Stubby on `localhost:9999`) all requests for `GET /public` but BLOCK requests for `GET /secret`:

```
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
```

Things to Note:

  - using `com.themillhousegroup.gatsby.GatsbyHttp.gatsbyHttp` to automagically create stubby endpoints
  - the `stubby` assertions can be used at the conclusion of the simulation, in addition to the standard `check`s during the scenario

#### Credits

  - [Gatling](http://gatling.io/)
  - [Stubby](https://github.com/headexplodes/http-stub-server-scala)

