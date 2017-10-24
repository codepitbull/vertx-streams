package io.vertx.lang.scala.reactivestreams

import java.util.concurrent.atomic.AtomicReference

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.api.{Sink, Source, TokenSubscription}
import org.reactivestreams.{Publisher, Subscriber, Subscription}

class SourcePublisher[T](src: Source[T])(implicit ec: VertxExecutionContext) extends Publisher[T]{

  @volatile var subscriber:Subscriber[_ >: T] = _

  override def subscribe(s: Subscriber[_ >: T]) = this.synchronized {
    if(subscriber == null) {
      subscriber = s
      src.subscribe(new HiddenSink())
    }
    else {
      s.onError(new IllegalArgumentException("Tried to subscribe twice"))
    }
  }

  class HiddenSink extends Sink[T] {

    override def onError(t: Throwable): Unit = subscriber.onError(t)

    override def onComplete(): Unit = subscriber.onComplete()

    override def onNext(t: T): Unit = subscriber.onNext(t)

    override def onSubscribe(s: TokenSubscription): Unit = {

      var done = false
      var requested = 0l

      val subscription = new Subscription {

        override def cancel(): Unit = {
          subscriber = null
          s.cancel()
        }

        override def request(n: Long): Unit = {
          if(subscriber == null) {
          }
          else if(n < 0) {
            subscriber.onError(new IllegalArgumentException(s"Requested a negative amount of $n"))
          }
          else {
            if(done) {
              ec.execute(() => s.request(n))
            }
            else {
              requested = n
            }
          }
        }
      }
      subscriber.onSubscribe(subscription)
      //Execution of request on the actual Source has to be delayed as otherwise the source might start
      //emitting item before onSubscribe has returned.
      // See rule in https://github.com/reactive-streams/reactive-streams-jvm#api-components
      done = true
      if(requested > 0) {
        s.request(requested)
      }
    }
  }
}
