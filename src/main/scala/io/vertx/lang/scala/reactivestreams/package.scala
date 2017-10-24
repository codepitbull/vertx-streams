package io.vertx.lang.scala

import io.reactivex.Flowable.fromPublisher
import io.vertx.lang.scala.streams.api.StreamStage
import io.vertx.scala.core.streams.WriteStream
import org.reactivestreams.{Publisher, Subscriber}

import scala.concurrent.Future

package object reactivestreams {

  implicit class ReactiveFuture[O](val future: Future[O])(implicit ec: VertxExecutionContext) {
    def flowable() = fromPublisher(new FuturePublisher(future))
  }

  /**
    * Extends [[WriteStream]]s with a subscriber-method to provide an entry point for reactive streams.
    * @param ws the WriteStream to extends
    * @tparam I type of elements consumed by the WriteStream
    */
  implicit class WriteStreamSubscriberExtender[I](val ws: WriteStream[I])(implicit ec:VertxExecutionContext) {
    def subscriber(batchSize: Long = 10): Subscriber[I] = new WriteStreamSubscriber[I](ws, batchSize)
  }


  /**
    * Extends [[Publisher]]s with a stream-method to provide a convenient entry-point for streams.
    * @param pub the [[Publisher]] to extend
    * @param ec the [[VertxExecutionContext]] all operations run on
    * @tparam O the output type of the [[Publisher]]
    */
  implicit class PublisherExtender[O](val pub: Publisher[O])(implicit ec:VertxExecutionContext) {
    def stream: StreamStage[Unit, O] = StreamStage[Unit, O](_ => new PublisherSource[O](pub))
  }

}
