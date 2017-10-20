package io.vertx.lang.scala.streams.api

/**
  * Basis for simple sources. [TokenSubscription]] handling is implemented.
  * The [[Source]] will pause if tokens run out and restart if a new set arrives.
  *
  * @tparam O outgoing event type
  */
trait SimpleSource[O] extends Source[O]{
  protected var subscription: TokenSubscription = _
  protected var subscriber: Sink[O] = _
  protected var remainingTokens:Long = 0

  override def subscribe(s: Sink[O]): Unit = {
    if (subscription != null)
      throw new IllegalArgumentException("This Source only supports one TokenSubscription at a time")
    subscriber = s
    subscription = new TokenSubscription {
      private var cancelled = false

      override def cancel(): Unit = {
        remainingTokens = 0
        subscriber = null
        subscription = null
        cancelled = true
      }

      override def request(n: Long): Unit = {
        if(!cancelled) {
          if(n <= 0) {
            subscriber.onError(new IllegalArgumentException(s"Requested $n-tokens "))
          }
          else {
            val oldValue = remainingTokens
            remainingTokens += n
            if(oldValue == 0) {
              start()
            }
          }
        }
      }
    }
    subscriber.onSubscribe(subscription)
    start()
  }

  /**
    * Will be called right after starting the streams and then each time we ran out of receiveTokens
    * and received new ones.
    */
  def start(): Unit
}
