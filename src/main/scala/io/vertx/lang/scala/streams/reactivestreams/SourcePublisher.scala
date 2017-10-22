package io.vertx.lang.scala.streams.reactivestreams

import java.util.concurrent.atomic.AtomicReference

import io.vertx.lang.scala.streams.api.{Sink, Source, TokenSubscription}
import org.reactivestreams.{Publisher, Subscriber, Subscription}

class SourcePublisher[T](src: Source[T]) extends Publisher[T]{

  var subscriber = new AtomicReference[Subscriber[_ >: T]]()

  override def subscribe(s: Subscriber[_ >: T]) = this.synchronized {
    if(subscriber.compareAndSet(null, s)) {
      src.subscribe(new HiddenSink())
    }
    else {
      s.onError(new IllegalArgumentException("Tried to subscribe twice"))
    }
  }

  class HiddenSink extends Sink[T] {

    override def onError(t: Throwable): Unit = subscriber.get().onError(t)

    override def onComplete(): Unit = subscriber.get().onComplete()

    override def onNext(t: T): Unit = subscriber.get().onNext(t)

    override def onSubscribe(s: TokenSubscription): Unit = {
      val subscription = new Subscription {

        override def cancel(): Unit = {
          subscriber.set(null)
          s.cancel()
        }

        override def request(n: Long): Unit = {
          if(subscriber == null) {
            //required_spec317_mustNotSignalOnErrorWhenPendingAboveLongMaxValue
          }
          else if(n < 0) {
            subscriber.get().onError(new IllegalArgumentException(s"Requests a negative amount of $n"))
          }
          else {
            s.request(n)
          }
        }
      }
      subscriber.get().onSubscribe(subscription)
    }
  }
}
