package com.themillhousegroup.gatsby.test

import akka.actor.ActorSystem

trait ActorScope extends org.specs2.mutable.After {
  implicit val system = ActorSystem.create("ActorScope")

  def after = {
    system.shutdown

    system.awaitTermination
  }
}
