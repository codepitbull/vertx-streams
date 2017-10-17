package io.vertx.lang.scala.streams.source

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.TestFunctionSink
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
@RunWith(classOf[JUnitRunner])
class VertxListSourceTest extends AsyncFlatSpec with Matchers with Assertions {
  "Streaming a List in Vert.x" should "work" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val testFunctionSink = TestFunctionSink(5)

    val source = new VertxListSource[Int](List(1, 2, 3, 5, 8))

    ec.execute(() =>
      source.subscribe(testFunctionSink.sink)
    )

    testFunctionSink.promise.future.map(s => s should equal(List(1, 2, 3, 5, 8)))
  }
}
