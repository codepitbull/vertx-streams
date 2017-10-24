package io.vertx.lang.scala.reactivestreams

import io.vertx.lang.scala.streams.api.{Sink, Source, TokenSubscription}
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.concurrent.ExecutionContext

/**
  * A [[Source]] implementation that allows to use a Reactive Streams [[Publisher]] to be used as the starting point
  * for s stream
  * @param publisher the [[Publisher]] to be wrapped
  * @param ec execution context to run all operations on
  * @tparam O outgoing event type
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class PublisherSource[O](publisher: Publisher[O])(implicit ec: ExecutionContext) extends Source[O] {
  protected var subscription: TokenSubscription = _
  protected var subscriber: Sink[O] = _

  private var reactiveStreamsSubscription:Subscription = _
  private var requested:Long = 0

  //All methods in Subscriber might be called on a different thread so I delegate the actual action back to the
  //event loop!
  publisher.subscribe(new Subscriber[O] {
    override def onError(t: Throwable): Unit = ec.execute(() => subscriber.onError(t))

    override def onComplete(): Unit = ec.execute(() => subscriber.onComplete())

    override def onNext(t: O): Unit = ec.execute(() => subscriber.onNext(t))

    override def onSubscribe(s: Subscription): Unit = ec.execute(() => {
      reactiveStreamsSubscription = s
      if(requested > 0) {
        reactiveStreamsSubscription.request(requested)
        requested = 0
      }
    })
  })

  override def subscribe(s: Sink[O]): Unit = {
    if (subscription != null)
      throw new IllegalArgumentException("This Source only supports one TokenSubscription at a time")
    subscriber = s
    subscription = new TokenSubscription {
      override def cancel(): Unit = reactiveStreamsSubscription.cancel()

      override def request(n: Long): Unit =
        if(reactiveStreamsSubscription != null)
          reactiveStreamsSubscription.request(n)
        else
          requested = requested + n

    }
    subscriber.onSubscribe(subscription)
  }
}

