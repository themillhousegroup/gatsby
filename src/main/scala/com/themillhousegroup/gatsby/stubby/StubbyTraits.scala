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

  import scala.concurrent.ExecutionContext.Implicits.global
  def acquireLock(taskName: String): Future[Boolean] = {
    logger.debug(s"acquireLock entered for $taskName")
    // hack impl
    Future {
      while (!token.compareAndSet(false, true)) {
        logger.debug(s"Awaiting lock for $taskName")
        Thread.sleep(1000)
      }

      logger.debug(s"ACQUIRED Lock for $taskName")
      true
    }
  }

  def releaseLock(taskName: String) = {
    logger.debug(s"Releasing lock for $taskName")
    token.set(false)
  }
}

trait RuntimeStubbing extends CanAddStubExchanges with CanRemoveStubExchanges with EnforcesMutualExclusion with HasLogger
