package com.themillhousegroup.gatsby.stubby

import org.slf4j.LoggerFactory
import com.dividezero.stubby.core.model.StubExchange
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.Future

trait HasLogger {
  lazy val logger = LoggerFactory.getLogger(getClass)
}

trait HasStubbyServer {
  val stubbyServer: StubbyServer
}

trait CanAddStubExchanges {
  /**
   * @return true if the exchange was added
   */
  def addExchange(requestName: String, se: StubExchange): Boolean
}

trait CanRemoveStubExchanges {
  /**
   * @return true at least one exchange was removed
   */
  def removeExchange(prefix: String): Boolean
}

trait EnforcesMutualExclusion {
  this: HasLogger =>

  val token = new AtomicBoolean(false)
  val loopWaitMillis = 1000
  var currentLockHolder: Option[String] = None

  import scala.concurrent.ExecutionContext.Implicits.global
  def acquireLock(taskName: String): Future[Boolean] = {
    logger.debug(s"acquireLock entered for $taskName")
    // hack impl
    Future {
      while (!token.compareAndSet(false, true)) {
        logger.debug(s"Awaiting lock for $taskName because the current holder is $currentLockHolder")
        Thread.sleep(loopWaitMillis)
      }

      logger.debug(s"ACQUIRED Lock for $taskName")
      currentLockHolder = Some(taskName)
      true
    }
  }

  def releaseLock(taskName: String): Boolean = {
    currentLockHolder.filter(_ == taskName).fold {
      logger.warn(s"Can't release lock; $taskName is not the holder")
      false
    } { holder =>
      logger.debug(s"Releasing lock for $holder")
      currentLockHolder = None
      token.set(false)
      true
    }
  }

}

trait RuntimeStubbing extends CanAddStubExchanges with CanRemoveStubExchanges with EnforcesMutualExclusion with HasLogger
