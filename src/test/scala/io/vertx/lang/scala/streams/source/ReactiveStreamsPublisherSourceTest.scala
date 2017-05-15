package io.vertx.lang.scala.streams.source

import java.util.concurrent.Executors

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.Stream._
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.reactivestreams.example.unicast.AsyncIterablePublisher
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Promise

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class ReactiveStreamsPublisherSourceTest extends AsyncFlatSpec with Matchers with Assertions {
  "A ReactiveStreams based Publisher" should "work as a Source in a stream" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val prom = Promise[List[Int]]
    val received = mutable.ListBuffer[Int]()

    vertx.eventBus()
      .localConsumer[Int]("sinkAddress")
      .handler(m => {
        received += m.body()
        if (received.size == 5)
          prom.success(received.toList)
      })

    ec.execute(() => {
      val producer = vertx.eventBus().sender[Int]("sinkAddress")

      val publisher = new AsyncIterablePublisher[Int](List(1, 2, 3, 4, 5).asJava, Executors.newFixedThreadPool(5))
      publisher.stream
        .sink(producer)
        .run()
    })

    prom.future.map(s => s should equal(List(1, 2, 3, 4, 5)))

  }
}
