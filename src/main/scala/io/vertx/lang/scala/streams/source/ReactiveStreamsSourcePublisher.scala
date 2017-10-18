package io.vertx.lang.scala.streams.source

import io.vertx.lang.scala.streams.api.{Sink, Source, TokenSubscription}
import org.reactivestreams.{Publisher, Subscriber, Subscription}

class ReactiveStreamsSourcePublisher[T](src: Source[T]) extends Publisher[T]{

  override def subscribe(s: Subscriber[_ >: T]) = {
    src.subscribe(new HiddenSink(s))
  }

  class HiddenSink(subscriber: Subscriber[_ >: T]) extends Sink[T] {

    override def onError(t: Throwable): Unit = subscriber.onError(t)

    override def onComplete(): Unit = subscriber.onComplete()

    override def onNext(t: T): Unit = subscriber.onNext(t)

    override def onSubscribe(s: TokenSubscription): Unit = {

      val subscription = new Subscription {

        override def cancel(): Unit = {
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
