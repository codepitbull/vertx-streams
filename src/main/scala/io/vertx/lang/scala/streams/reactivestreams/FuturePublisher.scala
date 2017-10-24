package io.vertx.lang.scala.streams.reactivestreams

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Reactive streams publisher based on a [[Future]]. Should cover all cases in Vert.x
  * Warning: Cancelling the Subscription <b>WILL NOT</b> cancel the future!
  * @param future the future we subscribe
  * @tparam T the value produced by the future
  */
class FuturePublisher[T](future: Future[T])(implicit ec: ExecutionContext) extends Publisher[T]{
  @volatile var subscriberRef:Subscriber[_ >: T] = _
  val requested = new AtomicBoolean(false)
  override def subscribe(subscriber: Subscriber[_ >: T]) = {
    if(subscriberRef == null){
      subscriberRef = subscriber
      subscriber.onSubscribe(new Subscription {
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
