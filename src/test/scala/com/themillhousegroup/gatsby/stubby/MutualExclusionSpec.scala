package com.themillhousegroup.gatsby.stubby

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.apache.commons.lang3.time.StopWatch
import scala.concurrent.{ Future, Await }
import scala.concurrent.duration.Duration
import org.slf4j.Logger

class MutualExclusionSpec extends Specification with Mockito {

  val maxWait = Duration(5, "seconds")

  class TestMutex extends EnforcesMutualExclusion with HasLogger {
    override lazy val logger = mock[Logger]
  }

  "EnforcesMutualExclusion trait" should {

    "Allow direct access when there is no contention for the locked resource" in {

      val t = new TestMutex()

      val hadToWait = t.acquireLock("direct")

      val hadToWaitForLock = Await.result(hadToWait, maxWait)

      t.currentLockHolder must beSome("direct")

      t.releaseLock("direct")

      t.currentLockHolder must beNone

      hadToWaitForLock must beFalse
    }

    "Force the second in line to wait" in {

      val t = new TestMutex()

      val firstLock = t.acquireLock("1")

      Thread.sleep(10)
      val secondLock = t.acquireLock("2")

      Await.ready(firstLock, maxWait)

      secondLock.isCompleted must beFalse

      t.currentLockHolder must beSome("1")

      t.releaseLock("1")

      t.currentLockHolder must beNone

      Await.ready(secondLock, maxWait)

      secondLock.isCompleted must beTrue

      t.releaseLock("2")

      t.currentLockHolder must beNone

    }

    "Only allow the holder of the lock to release it" in {

      val t = new TestMutex()

      Await.ready(t.acquireLock("owner"), maxWait)

      t.currentLockHolder must beSome("owner")

      t.releaseLock("other") must beFalse

      t.currentLockHolder must beSome("owner")

      t.releaseLock("owner") must beTrue

      t.currentLockHolder must beNone
    }

    "Return false if the lock is released before it is acquired" in {

      val t = new TestMutex()

      t.releaseLock("wrong") must beFalse
    }
  }
}
