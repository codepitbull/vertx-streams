package io.vertx.lang.scala.streams.source

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.TestFunctionSink
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class ReadStreamSourceTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a ReadStream as a Source" should "work" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val testFunctionSink = TestFunctionSink(5)

    val readStream = vertx.eventBus()
      .consumer[Int]("testAddress")
      .bodyStream()

    new ReadStreamSource[Int](readStream)
      .subscribe(testFunctionSink.sink)

    val sender = vertx.eventBus().sender[Int]("testAddress")
    sender.send(1)
    sender.send(2)
    sender.send(3)
    sender.send(5)
    sender.send(8)

    testFunctionSink.promise.future.map(s => s should equal(List(1, 2, 3, 5, 8)))
  }
}
