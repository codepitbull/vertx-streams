package io.vertx.lang.scala.streams.source

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.sink.FunctionSink
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
class VertxListSourceTest extends AsyncFlatSpec with Matchers with Assertions {
  "Streaming a List in Vert.x" should "work" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val prom = Promise[List[Int]]

    val original = List(1, 2, 3, 5, 8)

    ec.execute(() => {
      val streamed = mutable.Buffer[Int]()

      val source = new VertxListSource[Int](original)
      val sink = new FunctionSink[Int](f => {
        streamed += f
        if (streamed.size == 5)
          prom.success(streamed.toList)
      })

      source.subscribe(sink)
    })

    prom.future.map(s => s should equal(original))
  }
}
