package io.vertx.lang.scala.streams.stage

import java.util.concurrent.atomic.AtomicInteger

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.TestFunctionSink
import io.vertx.lang.scala.streams.source.VertxListSource
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class ProcessStageTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a ProcessStage" should "not change the events" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)
    val testFunctionSink = TestFunctionSink(5)

    val sideEffectAccumulator = new AtomicInteger(0)

    val source = new VertxListSource[Int](List(1, 2, 3, 5, 8))
    val processStage = new ProcessStage((i: Int) => sideEffectAccumulator.addAndGet(i))

    processStage.subscribe(testFunctionSink.sink)
    source.subscribe(processStage)


    testFunctionSink.promise.future
      .map(s => s should equal(List(1, 2, 3, 5, 8)))
      .map(s => sideEffectAccumulator.get() should equal(19))
  }
}
