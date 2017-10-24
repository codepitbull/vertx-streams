package io.vertx.lang.scala.reactivestreams

import java.util.concurrent.atomic.AtomicReference

import io.vertx.lang.scala.streams.api.{Sink, TokenSubscription}
import io.vertx.lang.scala.{ScalaLogger, VertxExecutionContext}
import org.reactivestreams.{Subscriber, Subscription}

/**
  * A [[Sink]] based on a Reactive Streams [[Subscriber]]
  * @param subscriber the actual subscriber that is going to receive the events
  * @param ec the VertxExecutionContext that is used to interact with the stream
  * @tparam I incoming event type
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class SubscriberSink[I](subscriber: Subscriber[I])(implicit ec: VertxExecutionContext) extends Sink[I]{

  private val Log = ScalaLogger.getLogger(getClass.getName)

  private val subscription: AtomicReference[TokenSubscription] = new AtomicReference[TokenSubscription]()

  //All methods in Subscription might be called on a different thread so I delegate the actual action back to the
  //event loop!
  private val reactiveStreamsSubscription = new Subscription {
    override def cancel(): Unit = ec.execute(() => subscription.get().cancel())

    override def request(n: Long): Unit = ec.execute(() => ec.execute(() => subscription.get().request(n)))
  }

  override def onNext(t: I): Unit = {
    //TODO: maybe do that on a WorkerExecutor as it might block
    subscriber.onNext(t)
  }

  override def onSubscribe(s: TokenSubscription): Unit = {
    this.synchronized {
      if(subscription.get() == null) {
        subscriber.onSubscribe(reactiveStreamsSubscription)
        subscription.set(s)
      }
      else
        throw new RuntimeException("Sink already has a TokenSubscription")
    }

  }

  override def onComplete(): Unit = {
    Log.info("Stream has ended")
    subscriber.onComplete()
  }

  override def onError(t: Throwable): Unit = {
    Log.error("Received an error", t)
    subscriber.onError(t)
  }
}
