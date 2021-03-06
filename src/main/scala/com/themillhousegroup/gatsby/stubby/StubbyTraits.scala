package com.themillhousegroup.gatsby.stubby

import com.dividezero.stubby.core.model.StubExchange
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.Future
import com.typesafe.scalalogging.slf4j.Logging

trait CanAddStubExchanges {
  /**
   * @return true if the exchange was added
   */
  def addExchange(requestName: String, se: StubExchange) = addExchanges(requestName, Seq(se))
  def addExchanges(requestName: String, ses: Seq[StubExchange]): Boolean
}

trait CanRemoveStubExchanges {
  /**
   * @return true at least one exchange was removed
   */
  def removeExchange(prefix: String): Boolean
}

trait EnforcesMutualExclusion {
  this: Logging =>

  val token = new AtomicBoolean(false)
  val loopWaitMillis = 1000
  var currentLockHolder: Option[String] = None

  import scala.concurrent.ExecutionContext.Implicits.global

  /**
   * @return Future(true) if we got direct access, Future(false) if we had to wait
   */
  def acquireLock(taskName: String): Future[Boolean] = {
    logger.debug(s"acquireLock entered for $taskName")
    // hack impl
    Future {
      var hadToWait = false

      while (!token.compareAndSet(false, true)) {
        hadToWait = true
        logger.debug(s"Awaiting lock for $taskName because the current holder is $currentLockHolder")
        Thread.sleep(loopWaitMillis)

      }

      logger.debug(s"ACQUIRED Lock for $taskName")
      currentLockHolder = Some(taskName)
      hadToWait
    }
  }

  /**
   * @return true if we were the holder, and we released it
   */
  def releaseLock(taskName: String): Boolean = {
    currentLockHolder.filter(_ == taskName).fold {
      logger.warn(s"Can't release lock; $taskName is not the holder: $currentLockHolder")
      false
    } { holder =>
      logger.debug(s"Releasing lock for $holder")
      currentLockHolder = None
      token.set(false)
      true
    }
  }

}

trait RuntimeStubbing extends CanAddStubExchanges with CanRemoveStubExchanges with EnforcesMutualExclusion with Logging
