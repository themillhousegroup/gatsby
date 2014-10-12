gatsby
============================

__Gat__[ling] at the front, __s__[tub]__by__ round the back.

Put your network tool/middleware/whatever in the middle and verify it does what it should.

### Usage
Check out [Gatling's excellent documentation](http://gatling.io/docs/2.0.0/) to understand how to record and/or write files in the Gatling DSL. Once you're comfortable with that, just use some of the additional Gatsby classes to get the auto-Stubby working.

#### Examples
Check out the files in [user-files/simulations/gatsbyexamples](https://github.com/themillhousegroup/gatsby/tree/master/user-files/simulations/gatsbyexamples), but here are some highlights

##### A backend server that's always there throughout the Simulation
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

#### Credits

  - [Gatling](http://gatling.io/)
  - [Stubby](https://github.com/headexplodes/http-stub-server-scala)

