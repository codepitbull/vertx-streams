package io.vertx.lang.scala.streams.stage

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
class FilterStageTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a FilterStage" should "remove specific events from the stream" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val original = List(1, 2, 3, 5, 8, 9)
    val expected = List(1, 3, 5, 8, 9)

    val testFunctionSink = TestFunctionSink(5)
    val source = new VertxListSource[Int](original)
    val stage = new FilterStage((i: Int) => i != 2)

    ec.execute(() => {
      stage.subscribe(testFunctionSink.sink)
        source.subscribe(stage)
      }
    )


    testFunctionSink.promise.future.map(s => s should equal(expected))
  }
}
