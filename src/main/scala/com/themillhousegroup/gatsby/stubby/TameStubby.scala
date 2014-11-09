package com.themillhousegroup.gatsby.stubby

import unfiltered.netty.Http
import com.dividezero.stubby.standalone.{ Main, AppPlan, Server }
import java.io.File
import com.dividezero.stubby.core.model.{ StubParam, StubResponse, StubRequest, StubExchange }
import io.gatling.core.session.{ Session, Expression }
import com.dividezero.stubby.core.service.model.StubServiceExchange
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
class TameStubby(paths: String*) extends StubbyServer {
  private[this] val httpPromise = Promise[Http]
  val http: Future[Http] = httpPromise.future

  val server = new Server(paths.flatMap { n: String => Main.loadFolder(n) })

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

