package io.vertx.lang.scala.streams.stage

import io.vertx.lang.scala.streams.api.SimpleStage
import io.vertx.lang.scala.{ScalaLogger, VertxExecutionContext}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * A [[io.vertx.lang.scala.streams.api.Stage]] that performes an asynchronous side effect. It ensures that all resulting operations are executed on the correct
  * [[VertxExecutionContext]].
  * If a failure occurs a new token is propagated upstream to compensate the loss of a token.
  * @param f the function producing the [[Future]]
  * @param ec the [[VertxExecutionContext]] all resulting operations run on
  * @tparam I input event type
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class ProcessAsyncStage[I](f: I => Future[Unit])
                             (implicit ec: VertxExecutionContext) extends SimpleStage[I, I] {
  protected val Log: ScalaLogger = ScalaLogger.getLogger(getClass.getName)

  override def next(event: I): Unit = {
    f(event).onComplete {
      case Success(i) => subscriber.onNext(event)
      case Failure(t) => {
        Log.warn(s"Failed mapAsync for $event", t)
        subscriber.onError(t)
      }
    }
  }
}
