package io.vertx.lang.scala.streams.sink

import java.util

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.Stream._
import io.vertx.lang.scala.streams.source.VertxListSource
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.concurrent.Promise
import scala.collection.JavaConverters._

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class FunctionSinkTest extends AsyncFlatSpec with Matchers with Assertions {
  "A FunctionSink " should "work as a Sink in a stream" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val prom = Promise[List[Int]]

    val received = new util.ArrayList[Int]()

    val source = new VertxListSource[Int](List(1, 2, 3, 5, 8))
    val rsSubscriber = new FunctionSink[Int](a => {
      received.add(a)
      if(received.size() == 5)
        prom.success(received.asScala.toList)
    })

    source.stream
      .sink(rsSubscriber)
      .run()

    prom.future.map(s => s should equal(List(1, 2, 3, 5, 8)))
  }
}
