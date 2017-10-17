package io.vertx.lang.scala.streams.stage

import io.vertx.lang.scala.streams.api.SimpleStage
import io.vertx.lang.scala.{ScalaLogger, VertxExecutionContext}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * A [[io.vertx.lang.scala.streams.api.Stage]] that takes care of [[Future]]s. It ensures that all resulting operations are executed on the correct
  * [[VertxExecutionContext]].
  * If a failure occurs a new token is propagated upstream to compensate the loss of a token.
  * @param f the function producing the [[Future]]
  * @param ec the [[VertxExecutionContext]] all resulting operations run on
  * @tparam I input event type
  * @tparam O output event type
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class MapAsyncStage[I, O](f: I => Future[O])
                         (implicit ec: VertxExecutionContext) extends SimpleStage[I, O] {
  protected val Log: ScalaLogger = ScalaLogger.getLogger(getClass.getName)

  override def next(event: I): Unit = {
    f(event).onComplete {
      case Success(i) => subscriber.onNext(i)
      case Failure(t) =>
        Log.warn(s"Failed mapAsync for $event", t)
        receiveSubscription.request(1)
        subscriber.onError(t)
    }
  }
}
