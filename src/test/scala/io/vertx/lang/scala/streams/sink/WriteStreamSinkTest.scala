package io.vertx.lang.scala.streams.sink

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.TestFunctionSink
import io.vertx.lang.scala.streams.source.{ReadStreamSource, VertxListSource}
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class WriteStreamSinkTest extends AsyncFlatSpec with Matchers with Assertions {
  "A WriteStream based Sink" should "work as a Sink in a stream" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val testFunctionSink = TestFunctionSink(5)

    new ReadStreamSource[Int](vertx.eventBus().consumer[Int]("testAddress").bodyStream())
      .subscribe(testFunctionSink.sink)

    ec.execute(() =>
      new VertxListSource[Int](List(1, 2, 3, 5, 8))
        .subscribe(new WriteStreamSink[Int](vertx.eventBus().publisher[Int]("testAddress")))
    )


    testFunctionSink.promise.future.map(s => s should equal(List(1, 2, 3, 5, 8)))
  }
}
