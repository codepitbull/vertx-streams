package io.vertx.lang.scala.streams.source

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.api.SimpleSource

/**
  * A [[io.vertx.lang.scala.streams.api.Source]] that is based on a List.
  * @param list the list to use for event generation
  * @param ec [[scala.concurrent.ExecutionContext]] used to reschedule to the event loop
  * @tparam O outgoing event type
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
  */
class VertxListSource[O](list: List[O])(implicit ec:VertxExecutionContext) extends SimpleSource[O]{
  private var index: Int = 0
  override def start(): Unit = {
    if(remainingTokens > 0) {
      if(index == list.size) {
        remainingTokens = 0
        subscriber.onComplete()
      }
      else {
        subscriber.onNext(list(index))
        index += 1
        remainingTokens -= 1
        if(remainingTokens > 0)
          ec.execute(() => start())
      }
    }
  }

}
