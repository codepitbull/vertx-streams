package io.vertx.lang.scala.streams.sink

import java.util.concurrent.{CopyOnWriteArrayList, Executors}

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.Stream._
import io.vertx.lang.scala.streams.source.VertxListSource
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.reactivestreams.example.unicast.AsyncSubscriber
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.concurrent.Promise

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class ReactiveStreamsSubscriberSinkTest extends AsyncFlatSpec with Matchers with Assertions {
  "A ReactiveStreams based Subscriber" should "work as Sink in a stream" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val prom = Promise[List[Int]]

    val received = new CopyOnWriteArrayList[Int]()

    ec.execute(() => {
      val source = new VertxListSource[Int](List(1, 2, 3, 5, 8))
      val rsSubscriber = new AsyncSubscriber[Int](Executors.newFixedThreadPool(5)) {
        override def whenNext(element: Int): Boolean = {
          received.add(element)
          if(received.size() == 5)
            prom.success(received.asScala.toList)
          true
        }
      }

      source.stream
        .sink(rsSubscriber)
        .run()
    })

    prom.future.map(s => s should equal(List(1, 2, 3, 5, 8)))
  }
}
