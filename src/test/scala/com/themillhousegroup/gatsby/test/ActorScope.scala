package com.themillhousegroup.gatsby.test

import akka.actor.ActorSystem
import java.util.UUID

trait ActorScope extends org.specs2.mutable.After {

  val scopeId = UUID.randomUUID()
  implicit val system = ActorSystem.create(s"ActorScope--$scopeId")

  def after = {
    system.shutdown

    system.awaitTermination
  }
}
