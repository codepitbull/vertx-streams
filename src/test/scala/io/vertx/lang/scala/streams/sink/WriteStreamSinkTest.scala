package io.vertx.lang.scala.streams.sink

import java.util.concurrent.{CopyOnWriteArrayList, Executors}

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.Stream._
import io.vertx.lang.scala.streams.source.{ReadStreamSource, VertxListSource}
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.reactivestreams.example.unicast.AsyncSubscriber
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Promise

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class WriteStreamSinkTest extends AsyncFlatSpec with Matchers with Assertions {
  "A WriteStream based Sink" should "work as a Sink in a stream" in {
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
      val source = new VertxListSource[Int](List(1, 2, 3, 5, 8))
      val sink = vertx.eventBus().publisher[Int]("testAddress")

      source.stream
        .sink(sink)
        .run()
    })

    prom.future.map(s => s should equal(List(1, 2, 3, 5, 8)))
  }
}
