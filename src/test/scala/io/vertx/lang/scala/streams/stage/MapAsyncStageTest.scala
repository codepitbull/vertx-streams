package io.vertx.lang.scala.streams.stage

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
class MapAsyncStageTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a MapAsyncStage" should "transform events" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)
    val testFunctionSink = TestFunctionSink(5)

    val original = List(1, 2, 3, 5, 8)
    val expected = List(2, 4, 6, 10, 16)

    val source = new VertxListSource[Int](original)
    val stage = new MapAsyncStage((i: Int) => Future(i*2))

    ec.execute(() => {
      stage.subscribe(testFunctionSink.sink)
      source.subscribe(stage)
    })

    testFunctionSink.promise.future.map(s => s should equal(expected))
  }
}
