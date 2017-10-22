package io.vertx.lang.scala.streams

import io.reactivex.Flowable
import io.reactivex.Flowable.fromPublisher
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.api.{Component, Sink, Source}
import io.vertx.lang.scala.streams.reactivestreams.SourcePublisher
import org.reactivestreams.Publisher

/**
  * Represents a part of the stream.
  * This class is not thread safe!
  * @param componentFactory will be used to create the actual component when the stream is started
  * @param incoming references to the sources of incoming events for this component
  * @param outputs number of outputs to connect
  * @tparam I type of incoming events
  * @tparam O type of outgoing events
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
class StreamStage[I, O](componentFactory: Unit => Component, incoming: List[StreamStage[_, I]], outputs: Int = 1) {

  var component: Component = _
  var outputCount:Int = 0

  /**
    * Start the stream
    * @return a reference to the created component
    */
  def run(): Component = {
    val input = incoming.map(i => i.run().asInstanceOf[Source[I]])

    if(component == null)
      component = componentFactory(())

    outputCount = outputCount + 1
    if(outputCount == outputs) {
      component match {
        case sink: Sink[I] => input.foreach(source => source.subscribe(sink))
        case source: Source[_] => ()
        case _ => throw new IllegalArgumentException(s"An unknown type has been constructed: $component}")
      }
    }

    component
  }

  def publisher()(implicit ec: VertxExecutionContext): Publisher[O] = {

    val input = incoming.map(i => i.run().asInstanceOf[Source[I]])

    if(component == null)
      component = componentFactory(())

    if(!component.isInstanceOf[Source[I]]) {
      throw new RuntimeException("Thou shalt use A SOURCE !!!1!!1!!")
    }

    outputCount = outputCount + 1
    if(outputCount == outputs) {
      component match {
        case sink: Sink[I] => input.foreach(source => source.subscribe(sink))
        case source: Source[_] => ()
        case _ => throw new IllegalArgumentException(s"An unknown type has been constructed: $component}")
      }
    }

    new SourcePublisher[O](component.asInstanceOf[Source[O]])
  }

  def rxjava2()(implicit ec: VertxExecutionContext): Flowable[O] = {
    fromPublisher(publisher())
  }
}

object StreamStage{
  def apply[I,O](componentFactory: Unit => Component, incoming: List[StreamStage[_, I]] = List()): StreamStage[I, O] = new StreamStage(componentFactory, incoming)
}