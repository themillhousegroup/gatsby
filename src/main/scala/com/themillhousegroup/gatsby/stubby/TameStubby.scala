package com.themillhousegroup.gatsby.stubby

import unfiltered.netty.Http
import com.dividezero.stubby.standalone.{ Main, AppPlan, Server }
import com.dividezero.stubby.core.model.{ StubRequest, StubExchange }
import scala.concurrent.{ Promise, Future }
import scala.concurrent.ExecutionContext.Implicits.global

trait StubbyServer {
  def start(port: Int)
  def stop
  def addExchange(exch: StubExchange)
  def removeExchange(exch: StubExchange)
  def requestsSeen: Seq[StubRequest]
  def exchangesSeen: Seq[StubExchange]
}

/** This is ripped from stubby-standalone's Main object */
class TameStubby extends StubbyServer {
  private[this] val httpPromise = Promise[Http]
  val http: Future[Http] = httpPromise.future

  val server = new Server(Nil)

  def start(port: Int) = {
    httpPromise.success(Http(port).plan(new AppPlan(server)).beforeStop({ server.fileSource.monitor.stop() }))
    http.foreach(_.start)
  }

  def stop = {
    http.foreach(_.stop)
  }

  def addExchange(exch: StubExchange) = {
    server.service.addResponse(exch)
  }

  def removeExchange(exch: StubExchange) = {
    server.service.deleteResponse(exch)
  }

  def requestsSeen = {
    server.service.requests.toSeq
  }

  def exchangesSeen = {
    server.service.responses.map(_.exchange).toSeq
  }
}

