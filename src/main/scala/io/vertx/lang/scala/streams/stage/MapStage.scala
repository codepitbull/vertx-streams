package io.vertx.lang.scala.streams.stage

import io.vertx.lang.scala.streams.api.SimpleStage

import scala.util.{Failure, Success, Try}

/**
  * A [[io.vertx.lang.scala.streams.api.Stage]] for mapping incoming events to different outgoing events.
  * @param f the mapping function
  * @tparam I incoming event type
  * @tparam O outgoing event type produced by the mapping function
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class MapStage[I,O](f: I => O) extends SimpleStage[I,O]{
  override def next(event: I): Unit = {
    Try(f(event)) match {
      case Success(r) => subscriber.onNext(r)
      case Failure(t) => subscriber.onError(t)
    }
  }
}

object MapStage{
  def apply[I, O](f: I => O): MapStage[I, O] = new MapStage(f)
}
