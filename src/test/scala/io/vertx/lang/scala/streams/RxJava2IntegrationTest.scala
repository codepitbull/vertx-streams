package io.vertx.lang.scala.streams

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

    val result = Promise[String]
    vertx.eventBus()
      .localConsumer[String]("targetAddress")
      .handler(m => result.success(m.body()))

    vertx
      .deployVerticleFuture(nameForVerticle[ReactiveStreamsVerticle])
      .map(s => vertx.eventBus().send("sourceAddress", "World"))

    result.future.map(r => r should equal("Hello World from RxJava2 !!!"))
  }
}


class ReactiveStreamsVerticle extends ScalaVerticle {
  override def startFuture() = {
    val consumer = vertx.eventBus().consumer[String]("sourceAddress")
    val sender = vertx.eventBus().sender[String]("targetAddress")

    consumer.bodyStream()
      .stream
      .map((a:String) => s"Hello $a")
      .rxjava2()
      .map[String](a => s"$a from RxJava2 !!!")
      .subscribe(sender.subscriber())

    consumer.completionFuture()
  }
}