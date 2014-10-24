package com.themillhousegroup.gatsby

import unfiltered.netty.Http
import com.dividezero.stubby.standalone.{ Main, AppPlan, Server }
import java.io.File
import com.dividezero.stubby.core.model.{ StubRequest, StubExchange }

trait StubbyServer {
  def start(port: Int)
  def stop
  def addExchange(exch: StubExchange)
  def removeExchange(exch: StubExchange)
  def requestsSeen: Seq[StubRequest]
}

/** This is ripped from stubby-standalone's Main object */
class TameStubby(paths: String*) extends StubbyServer {
  var http: Option[Http] = None

  val server = new Server(paths.flatMap { n: String => Main.loadFolder(n) })

  def start(port: Int) = {
    http = Some(Http(port).plan(new AppPlan(server)).beforeStop({ server.fileSource.monitor.stop() }))
    http.get.start()
  }

  def stop = {
    http.get.stop()
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
}
