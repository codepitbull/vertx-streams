package io.vertx.lang.scala

import io.vertx.lang.scala.reactivestreams.SubscriberSink
import io.vertx.lang.scala.streams.api.{Sink, Source, StreamStage}
import io.vertx.lang.scala.streams.sink.{FunctionSink, WriteStreamSink}
import io.vertx.lang.scala.streams.source.{FutureSource, ReadStreamSource}
import io.vertx.lang.scala.streams.stage._
import io.vertx.scala.core.streams.{ReadStream, WriteStream}
import org.reactivestreams.Subscriber

import scala.concurrent.Future

/**
  * All sorts of helpers and extensions to allow easy creations of Streams.
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
package object streams {
  /**
    * Extends [[Future]]s with a stream-method to provide a convenient entry-point for streams.
    * @param future the Future to extends
    * @tparam O type of elements produced by the stream
    */
  implicit class FutureSourceExtender[O](val future: Future[O])(implicit ec: VertxExecutionContext) {
    def stream: StreamStage[Unit, O] = StreamStage[Unit, O](_ => new FutureSource[O](future))
  }

  /**
    * Extends [[Source]]s with a stream-method to provide a convenient entry-point for streams.
    * @param src the Source to extends
    * @tparam O type of elements produced by the stream
    */
  implicit class SourceExtender[O](val src: Source[O]) {
    def stream: StreamStage[Unit, O] = StreamStage[Unit, O](_ => src)
  }

  /**
    * Extends [[ReadStream]]s with a stream-method to provide a convenient entry-point for streams.
    * @param rs the ReadStream to extends
    * @tparam O type of elements produced by the stream
    */
  implicit class ReadStreamSourceExtender[O](val rs: ReadStream[O]) {
    def stream: StreamStage[Unit, O] = StreamStage[Unit, O](_ => new ReadStreamSource[O](rs))
  }

  /**
    * Extend all [[io.vertx.lang.scala.streams.api.Source]]s with a set of methods to fluentyl create a stream.
    * @param streamBuilder the starting point of the stream
    * @tparam I type of events produced by the [[io.vertx.lang.scala.streams.api.Source]]
    */
  implicit class StreamBuilderExtender[I](val streamBuilder: StreamStage[_,I]) {
    /**
      * Map from one event rype to another.
      * @param f mapping function
      * @tparam O outgoing event type
      * @return a new source to attach further operations to
      */
    def map[O](f: I => O): StreamStage[I,O] =
      StreamStage[I,O](_ => new MapStage[I, O](f), streamBuilder :: Nil)

    /**
      * Remove all events from the stream that don't match a given predicate
      * @param f predicate function
      * @return a new source to attach further operations to
      */
    def filter(f: I => Boolean): StreamStage[I,I] =
      StreamStage[I,I](_ => new FilterStage[I](f), streamBuilder :: Nil)

    /**
      * Incoming events are mapped to a [[Future]]. The resulting [[Future]] is evaluated and its result propagated to
      * the stream.
      * @param f fucntion to map incoming value to a [[Future]]
      * @param failureHandler called if the [[Future]] fails
      * @param ec male sure all operations run on the [[VertxExecutionContext]]
      * @tparam O type of resulting events
      * @return a new source to attach further operations to
      */
    def mapAsync[O](f: I => Future[O], failureHandler: (I, Throwable) => Unit = (a: I, t: Throwable) => {})(implicit ec: VertxExecutionContext): StreamStage[I,O] =
      StreamStage[I,O](_ => new MapAsyncStage[I, O](f), streamBuilder :: Nil)

    /**
      * Incoming events are processed asynchronously.
      * @param f fucntion producing a [[Future]]
      * @param failureHandler called if the [[Future]] fails
      * @param ec male sure all operations run on the [[VertxExecutionContext]]
      * @return a new source to attach further operations to
      */
    def processAsync(f: I => Future[Unit], failureHandler: (I, Throwable) => Unit = (a: I, t: Throwable) => {})(implicit ec: VertxExecutionContext): StreamStage[I,I] =
      StreamStage[I,I](_ => new ProcessAsyncStage[I](f), streamBuilder :: Nil)

    /**
      * Execute a given function as a side effect.
      * @param f the function to execute as a side effect
      * @return a new source to attach further operations to
      */
    def process(f: I => Unit): StreamStage[I,I] =
      StreamStage[I,I](_ => new ProcessStage[I](f), streamBuilder :: Nil)

    /**
      * Create an endpoint for the stream from a given function.
      * @param f a function to receive all events from the stream
      */
    def sink(f: I => Unit, batchSize: Long = 1): StreamStage[I,I] =
      StreamStage[I,I](_ => new FunctionSink[I](f, batchSize), streamBuilder :: Nil)

    /**
      * Create an endpoint for the stream from a given [[WriteStream]].
      * @param ws a [[WriteStream]] to receive all events from the stream
      */
    def sink(ws: WriteStream[I]): StreamStage[I,I] =
      StreamStage[I,I](_ => new WriteStreamSink[I](ws), streamBuilder :: Nil)

    /**
      * Create an endpoint for the stream from a given [[Sink]].
      * @param sink a [[Sink]] to receive all events from the stream
      */
    def sink(sink: Sink[I]): StreamStage[I,I] =
      StreamStage[I,I](_ => sink, streamBuilder :: Nil)

    /**
      * Create an endpoint for the stream from a given [[Sink]].
      * @param sink a [[Sink]] to receive all events from the stream
      */
    def sink(sink: Subscriber[I])(implicit ec:VertxExecutionContext): StreamStage[I,Unit] =
      StreamStage[I, Unit](_ => new SubscriberSink[I](sink), streamBuilder :: Nil)
  }

}
