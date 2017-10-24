package io.vertx.lang.scala.streams

import java.util.concurrent.atomic.AtomicInteger

import io.vertx.lang.scala.ScalaVerticle.nameForVerticle
import io.vertx.lang.scala.streams.Stream._
import io.vertx.lang.scala.{ScalaVerticle, VertxExecutionContext}
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.concurrent.Promise

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
@RunWith(classOf[JUnitRunner])
class RxJava2IntegrationTest extends AsyncFlatSpec with Matchers with Assertions {

  "Using Vert.x-streams" should "work as Publishers in RxJava2" in {
    val vertx = Vertx.vertx
    implicit val exec = VertxExecutionContext(vertx.getOrCreateContext())

    val result = Promise[Int]
    val counter = new AtomicInteger(0)

    val iterations = 30
    vertx.eventBus()
      .localConsumer[String]("targetAddress")
      .handler(m =>
        if("Hello World from RxJava2 !!!".equals(m.body())){
          if(iterations == counter.incrementAndGet()) {
            result.success(counter.get())
          }
        }
      )

    vertx
      .deployVerticleFuture(nameForVerticle[ReactiveStreamsVerticle])
      .map(s => (1 to iterations).foreach(i => vertx.eventBus().send("sourceAddress", "World")))

    result.future.map(r => r should equal(iterations))
  }
}


class ReactiveStreamsVerticle extends ScalaVerticle {
  override def startFuture() = {
    val consumer = vertx.eventBus().consumer[String]("sourceAddress")
    val sender = vertx.eventBus().sender[String]("targetAddress")

    consumer.bodyStream()
      .stream
      .map((a:String) => s"Hello $a")
      .flowable()
      .map[String](a => s"$a from RxJava2 !!!")
      .subscribe(sender.subscriber())

    consumer.completionFuture()
  }
}