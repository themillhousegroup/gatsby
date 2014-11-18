package com.themillhousegroup.gatsby.test

import akka.actor.Actor
import scala.concurrent.{Future, Promise}
import io.gatling.core.session.Session

class NextActor extends Actor {

  private[this] val notificationPromise = Promise[Session]
  val notified: Future[Session] = notificationPromise.future

  def receive: Actor.Receive = {
    case s: Session => notificationPromise.success(s)
    case x: Any => notificationPromise.failure(new IllegalArgumentException(s"Wanted a Session, got: $x"))
  }
}
