package io.vertx.lang.scala.streams

import io.vertx.scala.core.eventbus.Message
import io.vertx.lang.scala.ScalaVerticle.nameForVerticle
import io.vertx.lang.scala.streams.Stream._
import io.vertx.lang.scala.{ScalaVerticle, VertxExecutionContext}
import io.vertx.scala.core.Vertx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.concurrent.Promise

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
@RunWith(classOf[JUnitRunner])
class StreamTest extends AsyncFlatSpec with Matchers with Assertions {

  "Transforming events in a stream" should "work" in {
    val vertx = Vertx.vertx
    implicit val exec = VertxExecutionContext(vertx.getOrCreateContext())

    val sinkAddress = Promise[String]
    vertx.eventBus()
      .localConsumer[String]("sinkAddress")
      .handler(m => sinkAddress.success(m.body()))

    vertx
      .deployVerticleFuture(nameForVerticle[NiceApiVerticle])
      .map(s => vertx.eventBus().send("sourceAddress", "World"))
    sinkAddress.future.map(r => r should equal("Hello World"))
  }

  "Using Futures in a stream" should "work" in {
    val vertx = Vertx.vertx
    implicit val exec = VertxExecutionContext(vertx.getOrCreateContext())

    val sinkAddress = Promise[String]
    vertx.eventBus()
      .localConsumer[String]("sinkAddress")
      .handler(m => sinkAddress.success(m.body()))

    vertx.eventBus()
      .localConsumer[String]("stageAddress")
      .handler(m => m.reply(s"saw ${m.body()}"))

    vertx
      .deployVerticleFuture(nameForVerticle[FutureTestVerticle])
      .map(s => vertx.eventBus().send("sourceAddress", "World"))
    sinkAddress.future.map(r => r should equal("saw World"))
  }

}

class NiceApiVerticle extends ScalaVerticle {
  override def startFuture() = {
    val consumer = vertx.eventBus().consumer[String]("sourceAddress")
    val producer = vertx.eventBus().sender[String]("sinkAddress")

    consumer.bodyStream()
      .stream
      .map((a:String) => s"Hello $a")
      .sink(producer)
      .run()

    consumer.completionFuture()
  }
}

class FutureTestVerticle extends ScalaVerticle {
  override def startFuture() = {
    val consumer = vertx.eventBus().consumer[String]("sourceAddress")
    val producer = vertx.eventBus().sender[String]("sinkAddress")

    consumer.bodyStream()
      .stream
      .mapAsync((a:String) => vertx.eventBus().sendFuture[String]("stageAddress", a))
      .mapAsync((a:Message[String]) => vertx.executeBlocking(() => a))
      .map(a => a.body())
      .sink(producer)
      .run()

    consumer.completionFuture()
  }
}
