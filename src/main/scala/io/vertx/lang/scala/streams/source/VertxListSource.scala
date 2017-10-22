package io.vertx.lang.scala.streams.source

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.api.SimpleSource

/**
  * A [[io.vertx.lang.scala.streams.api.Source]] that is based on a List.
  * @param list the list to use for event generation
  * @param ec [[scala.concurrent.ExecutionContext]] used to reschedule to the event loop
  * @tparam O outgoing event type
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class VertxListSource[O](list: List[O])(implicit ec:VertxExecutionContext) extends SimpleSource[O]{
  private var index: Int = 0
  private var paused = true
  override def tokensReceived(): Unit = {
    if(paused) {
      paused = false
      next()
    }
  }

  def next(): Unit = {
    if(index == list.size) {
      subscriber.onComplete()
    }
    else {
      ec.execute(() => {
        if(subscriber == null) {
          paused = true
        }
        else {
          remainingTokens = remainingTokens - 1
          subscriber.onNext(list(index))
          index += 1
          if(remainingTokens > 0) {
            next()
          }
          else {
            paused = true
          }
        }
      })
    }

  }
}
