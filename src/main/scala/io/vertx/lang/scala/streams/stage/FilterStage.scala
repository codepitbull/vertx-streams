package io.vertx.lang.scala.streams.stage

import io.vertx.lang.scala.streams.api.SimpleStage

import scala.util.{Failure, Success, Try}

/**
  * A Stage that will only allow events matching a certain predicate to pass.
  * If an event doesn't match it is discarded and a new token is propagated upstream to compensate the loss of a
  * token.
  * @param f the predicate
  * @tparam I incoming event type
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class FilterStage[I](f: I => Boolean) extends SimpleStage[I,I]{
  override def next(event: I): Unit = {
    Try(f(event)) match {
      case Success(true) => subscriber.onNext(event)
      case Success(false) => receiveSubscription.request(1)
      case Failure(t) => subscriber.onError(t)
    }
  }
}
