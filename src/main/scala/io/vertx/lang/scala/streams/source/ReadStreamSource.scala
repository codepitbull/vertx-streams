package io.vertx.lang.scala.streams.source

import io.vertx.lang.scala.streams.api.SimpleSource
import io.vertx.scala.core.streams.ReadStream

/**
  * An adaptor to allow [[ReadStream]]s to be used as the starting point of a stream.
  * @param rs the [[ReadStream]] to wrap
  * @tparam O outgoing event type
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class ReadStreamSource[O](rs: ReadStream[O]) extends SimpleSource[O]{

  private var paused = false

  rs.handler(event => {
    subscriber.onNext(event)
    remainingTokens -= 1
    if(remainingTokens == 0){
      rs.pause()
      paused = true
    }
  })

  override def tokensReceived(): Unit = {
    if(paused) {
      rs.resume()
      paused = false
    }
  }

}
