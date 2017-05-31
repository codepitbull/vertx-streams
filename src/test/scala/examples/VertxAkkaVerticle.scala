package examples

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.streams.Stream._
import org.reactivestreams.Publisher

/**
  * A [[ScalaVerticle]] that sends numbers produced by an akka-flow to
  * a eventbus address.
  */
class VertxAkkaVerticle extends ScalaVerticle{


  override def start(): Unit = {
    val producer = vertx.eventBus().sender[Int](VertxAkkaVerticle.receiverAddress)

    akkaFlow.stream
      .sink(producer)
      .run()
  }

  def akkaFlow: Publisher[Int] = {
    implicit val system = ActorSystem("reactive-streams-test")
    implicit val materializer = ActorMaterializer()
    val source: Source[Int, NotUsed] = Source(0 to 100)
      .map(a => a + 1)
    source.runWith(Sink.asPublisher(false))
  }
}

object VertxAkkaVerticle {
  val receiverAddress = "sinkAddress"
}
