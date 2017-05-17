package io.vertx.lang.scala.streams.sink

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
class FunctionSinkTest extends AsyncFlatSpec with Matchers with Assertions {
  "A FunctionSink " should "work as a Sink in a stream" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val testFunctionSink = TestFunctionSink(5)

    ec.execute(() =>
      new VertxListSource[Int](List(1, 2, 3, 5, 8))
        .subscribe(testFunctionSink.sink)
    )

    testFunctionSink.promise.future.map(s => s should equal(List(1, 2, 3, 5, 8)))
  }
}
