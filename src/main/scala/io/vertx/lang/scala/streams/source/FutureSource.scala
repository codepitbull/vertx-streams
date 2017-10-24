package io.vertx.lang.scala.streams.source

import java.util.concurrent.atomic.AtomicBoolean

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.api.{Sink, Source, TokenSubscription}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class FutureSource[T](future: Future[T])(implicit ec: VertxExecutionContext) extends Source[T]{
  @volatile var subscriberRef:Sink[_ >: T] = _
  val requested = new AtomicBoolean(false)

  override def subscribe(subscriber: Sink[T]): Unit = {
    if(subscriberRef == null){
      subscriberRef = subscriber
      subscriber.onSubscribe(new TokenSubscription {
        override def cancel(): Unit = {
          //The future can't be stopped
          subscriberRef = null
        }

        override def request(n: Long): Unit = {
          if(n <= 0) {
            subscriber.onError(new IllegalArgumentException(s"Requested $n-tokens "))
          }
          else {
            if(requested.compareAndSet(false,true)) {
              future.onComplete {
                case Success(res) =>  {
                  if(subscriberRef != null) {
                    subscriberRef.onNext(res)
                    subscriberRef.onComplete()
                  }
                }
                case Failure(t) => {
                  if(subscriberRef != null) {
                    subscriberRef.onError(t)
                  }
                }
              }
            }
          }
        }
      })
    }
    else {
      throw new IllegalArgumentException("Only one subscriber allowed")
    }
  }
}
