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
class ReadStreamSourceTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a ReadStream as a Source" should "work" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val prom = Promise[List[Int]]

    val streamed = mutable.Buffer[Int]()
    val readStream = vertx.eventBus()
      .consumer[Int]("testAddress")
      .bodyStream()
    val source = new ReadStreamSource[Int](readStream)
    val sink = new FunctionSink[Int](f => {
      streamed += f
      if (streamed.size == 5)
        prom.success(streamed.toList)
    })

    source.subscribe(sink)

    ec.execute(() => {
      val sender = vertx.eventBus().sender[Int]("testAddress")
      sender.send(1)
      sender.send(2)
      sender.send(3)
      sender.send(5)
      sender.send(8)
    })

    prom.future.map(s => s should equal(List(1, 2, 3, 5, 8)))
  }
}
