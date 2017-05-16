package io.vertx.lang.scala.streams.stage

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.TestFunctionSink
import io.vertx.lang.scala.streams.sink.FunctionSink
import io.vertx.lang.scala.streams.source.VertxListSource
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable
import scala.concurrent.Promise

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class MapStageTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a MapStage" should "transform events" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)
    val testFunctionSink = TestFunctionSink(5)

    val original = List(1, 2, 3, 5, 8)
    val expected = List(2, 4, 6, 10, 16)

    val source = new VertxListSource[Int](original)
    val stage = new MapStage((i: Int) => i * 2)

    stage.subscribe(testFunctionSink.sink)
    source.subscribe(stage)

    testFunctionSink.promise.future.map(s => s should equal(expected))
  }
}
