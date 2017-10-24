package io.vertx.lang.scala.streams.sink

import java.util.concurrent.{CopyOnWriteArrayList, Executors}

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.reactivestreams.SubscriberSink
import io.vertx.lang.scala.streams.source.VertxListSource
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.reactivestreams.example.unicast.AsyncSubscriber
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.concurrent.Promise

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
@RunWith(classOf[JUnitRunner])
class SubscriberSinkTest extends AsyncFlatSpec with Matchers with Assertions {
  "A ReactiveStreams based Subscriber" should "work as a Sink in a stream" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val prom = Promise[List[Int]]

    //Will be written from a different thread in AsyncSubscriber
    val received = new CopyOnWriteArrayList[Int]()

    val rsSubscriber = new AsyncSubscriber[Int](Executors.newFixedThreadPool(5)) {
      override def whenNext(element: Int): Boolean = {
        received.add(element)
        if(received.size() == 5)
          prom.success(received.asScala.toList)
        true
      }
    }

    ec.execute(() =>
      new VertxListSource[Int](List(1, 2, 3, 5, 8))
        .subscribe(new SubscriberSink[Int](rsSubscriber))
    )


    prom.future.map(s => s should equal(List(1, 2, 3, 5, 8)))
  }
}
