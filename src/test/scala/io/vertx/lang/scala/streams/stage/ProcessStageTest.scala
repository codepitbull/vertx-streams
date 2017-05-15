package io.vertx.lang.scala.streams.stage

import java.util.concurrent.atomic.AtomicInteger

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.sink.FunctionSink
import io.vertx.lang.scala.streams.source.VertxListSource
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
class ProcessStageTest extends AsyncFlatSpec with Matchers with Assertions {
  "Using a ProcessStage" should "not change the events" in {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = VertxExecutionContext(ctx)

    val counter = new AtomicInteger(0)
    val prom = Promise[List[Int]]

    ec.execute(() => {
      val streamed = mutable.Buffer[Int]()

      val source = new VertxListSource[Int](List(1, 2, 3, 5, 8))
      val processStage = new ProcessStage((i: Int) => counter.addAndGet(i))
      val sink = new FunctionSink[Int](f => {
        streamed += f
        if (streamed.size == 5)
          prom.success(streamed.toList)
      })

      processStage.subscribe(sink)
      source.subscribe(processStage)
    })


    prom.future
      .map(s => s should equal(List(1, 2, 3, 5, 8)))
      .map(s => counter.get() should equal(19))
  }
}
