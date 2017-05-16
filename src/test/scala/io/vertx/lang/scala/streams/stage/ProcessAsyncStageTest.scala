package io.vertx.lang.scala.streams.stage

import java.util.concurrent.atomic.AtomicInteger

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.TestFunctionSink
import io.vertx.lang.scala.streams.source.VertxListSource
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.concurrent.Future

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class ProcessAsyncStageTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a ProcessAsyncStage" should "not change the events" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val sideEffectCounter = new AtomicInteger(0)

    val testFunctionSink = TestFunctionSink(5)

    val source = new VertxListSource[Int](List(1, 2, 3, 5, 8))
    val processStage = new ProcessAsyncStage((i: Int) => Future(sideEffectCounter.addAndGet(i)))

    processStage.subscribe(testFunctionSink.sink)
    source.subscribe(processStage)


    testFunctionSink.promise.future
      .map(s => s should equal(List(1, 2, 3, 5, 8)))
      .map(s => sideEffectCounter.get() should equal(19))
  }
}
