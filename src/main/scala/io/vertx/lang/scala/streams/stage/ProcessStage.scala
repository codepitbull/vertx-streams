package io.vertx.lang.scala.streams.stage

import io.vertx.lang.scala.streams.api.SimpleStage

import scala.util.{Failure, Success, Try}

/**
  * A [[io.vertx.lang.scala.streams.api.Stage]] to execute a given function as a side effect without changing the stream.
  * @param f the function to execute
  * @tparam I the incoming event type
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class ProcessStage[I](f: I => Unit) extends SimpleStage[I,I]{
  override def next(event: I): Unit = {
    Try(f(event)) match {
      case Success(()) => subscriber.onNext(event)
      case Failure(t) => subscriber.onError(t)
    }

  }
}
