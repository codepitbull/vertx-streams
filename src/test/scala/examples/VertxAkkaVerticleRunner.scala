package examples

import io.vertx.lang.scala.ScalaVerticle.nameForVerticle
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.scala.core.Vertx

import scala.util.Success

object VertxAkkaVerticleRunner {
  def main(args: Array[String]): Unit = {
    val vertx = Vertx.vertx()
    implicit val ec = VertxExecutionContext(vertx.getOrCreateContext())
    vertx.eventBus()
      .consumer[Int](VertxAkkaVerticle.receiverAddress)
      .handler(msg => println(msg.body()))
      .completionFuture()
      .onComplete{
        case Success(_) => vertx.deployVerticle(nameForVerticle[VertxAkkaVerticle])
      }
  }
}
