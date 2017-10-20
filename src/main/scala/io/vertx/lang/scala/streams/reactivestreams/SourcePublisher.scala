package io.vertx.lang.scala.streams.reactivestreams

import io.vertx.lang.scala.streams.api.{Sink, Source, TokenSubscription}
import org.reactivestreams.{Publisher, Subscriber, Subscription}

class SourcePublisher[T](src: Source[T]) extends Publisher[T]{

  var subscriber: Subscriber[_ >: T] = _

  override def subscribe(s: Subscriber[_ >: T]) = this.synchronized {
    if(subscriber != null)
      subscriber.onError(new IllegalArgumentException("Tried to subscribe twice"))
    else {
      subscriber = s
      src.subscribe(new HiddenSink())
    }
  }

  class HiddenSink extends Sink[T] {

    override def onError(t: Throwable): Unit = subscriber.onError(t)

    override def onComplete(): Unit = subscriber.onComplete()

    override def onNext(t: T): Unit = subscriber.onNext(t)

    override def onSubscribe(s: TokenSubscription): Unit = {
      val subscription = new Subscription {

        override def cancel(): Unit = {
          subscriber = null
          s.cancel()
        }

        override def request(n: Long): Unit = {
          s.request(n)
        }
      }

      subscriber.onSubscribe(subscription)
    }
  }
}
