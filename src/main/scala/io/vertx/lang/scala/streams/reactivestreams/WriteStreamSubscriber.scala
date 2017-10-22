package io.vertx.lang.scala.streams.reactivestreams

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import io.vertx.lang.scala.{ScalaLogger, VertxExecutionContext}
import io.vertx.scala.core.streams.WriteStream
import org.reactivestreams.{Subscriber, Subscription}

/**
  * This [[io.vertx.lang.scala.streams.api.Sink]]-implementation takes a [[WriteStream]] for processing incoming events.
  * The Vert.x-API produces these in many places (including the Event Bus).
  *
  * @param ws the Stream to tokensReceived from
  * @param _batchSize size of batches that should be processed
  * @tparam I type of incoming events
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class WriteStreamSubscriber[I](ws: WriteStream[I], _batchSize: Long)(implicit ec: VertxExecutionContext) extends Subscriber[I]{

  protected val Log: ScalaLogger = ScalaLogger.getLogger(getClass.getName)

  val tokenCounter = new AtomicLong(0)

  var subscription = new AtomicReference[Subscription]

  override def onError(t: Throwable) = {
    if(t == null) {
      throw new NullPointerException("onError called with null as parameter")
    }
    ec.execute(() => {
      Log.error("Terminating stream due to an error", t)
      ws.end()
    })
  }

  override def onComplete() = {
    ec.execute(() => ws.end())
  }

  override def onNext(t: I) = {
    if(t == null) {
      throw new NullPointerException("onNext was called with null as param")
    }
    if(tokenCounter.decrementAndGet() < 0) {
      Log.error("Received a new item but there are no tokens left.")
      throw new RuntimeException("Received a new item but there are no tokens left.")
    }
    ec.execute(() => {
      if(tokenCounter.get() == 0) {
        tokenCounter.addAndGet(_batchSize)
        subscription.get().request(_batchSize)
      }
      ws.write(t)
    })
  }

  override def onSubscribe(s: Subscription) = {
    if(!subscription.compareAndSet(null, s)) {
      Log.error("Subscriber can only be subscribed once.")
      s.cancel()
    }
    else {
      tokenCounter.addAndGet(_batchSize)
      s.request(_batchSize)
    }
  }

}
