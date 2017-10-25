package io.vertx.lang.scala.streams.api

import io.reactivex.Flowable
import io.reactivex.Flowable.fromPublisher
import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.reactivestreams.SourcePublisher
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

  /**
    * Convert the stream into a reactive streams [[Publisher]]
    * @param ec Vert.x execution context to be used for the Publisher
    * @return the Publisher, usable with reactive streams
    */
  def publisher()(implicit ec: VertxExecutionContext): Publisher[O] = {

    val src = run()

    if(!src.isInstanceOf[Source[O]]) {
      throw new RuntimeException("Thou shalt use A SOURCE !!!1!!1!!")
    }

    new SourcePublisher[O](src.asInstanceOf[Source[O]])
  }

  /**
    * Convert the stream into a RxJava2 [[Flowable]]
    * @param ec Vert.x execution context to be used for the Publisher
    * @return the Flowable, usable wth RxJava2
    */
  def flowable()(implicit ec: VertxExecutionContext): Flowable[O] = {
    fromPublisher(publisher())
  }

}

object StreamStage{
  def apply[I,O](componentFactory: Unit => Component, incoming: List[StreamStage[_, I]] = List()): StreamStage[I, O] = new StreamStage(componentFactory, incoming)
}