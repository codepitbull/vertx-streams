package io.vertx.lang.scala.streams.stage

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.sink.FunctionSink
import io.vertx.lang.scala.streams.source.VertxListSource
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class MapAsyncStageTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a MapAsyncStage" should "transform events" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val prom = Promise[List[String]]

    val original = List(1, 2, 3, 5, 8)
    val expected = List("Int 1", "Int 2", "Int 3", "Int 5", "Int 8")

    ec.execute(() => {
      val streamed = mutable.Buffer[String]()

      val source = new VertxListSource[Int](original)
      val stage = new MapAsyncStage((i: Int) => Future(s"Int $i"))
      val sink = new FunctionSink[String](f => {
        streamed += f
        if (streamed.size == 5)
          prom.success(streamed.toList)
      })

      stage.subscribe(sink)
      source.subscribe(stage)
    })

    prom.future.map(s => s should equal(expected))
  }
}
