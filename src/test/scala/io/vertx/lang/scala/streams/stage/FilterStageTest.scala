package io.vertx.lang.scala.streams.stage

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.sink.FunctionSink
import io.vertx.lang.scala.streams.source.VertxListSource
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.collection.mutable
import scala.concurrent.Promise

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class FilterStageTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a FilterStage" should "remove specific events from the stream" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val prom = Promise[List[Int]]
    val original = List(1, 2, 3, 5, 8)
    val expected = List(1, 3, 5, 8)

    ec.execute(() => {
      val streamed = mutable.Buffer[Int]()

      val source = new VertxListSource[Int](original)
      val stage = new FilterStage((i: Int) => i != 2)
      val sink = new FunctionSink[Int](f => {
        streamed += f
        if (streamed.size == 4)
          prom.success(streamed.toList)
      })

      stage.subscribe(sink)
      source.subscribe(stage)
    })

    prom.future.map(s => s should equal(expected))
  }
}
