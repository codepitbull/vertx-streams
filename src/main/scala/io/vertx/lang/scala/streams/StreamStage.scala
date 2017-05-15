package io.vertx.lang.scala.streams

import io.vertx.lang.scala.streams.api.{Component, Sink, Source}

/**
  * Represents a part of the stream.
  * This class is not thrread safe!
  * @param componentFactory will be used to create the actual component when the stream is started
  * @param incoming references to the sources of incoming events for this component
  * @param outputs number of outputs to connect
  * @tparam I type of icnoming events
  * @tparam O type of outgoing events
  *
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a
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
}

object StreamStage{
  def apply[I,O](componentFactory: Unit => Component, incoming: List[StreamStage[_, I]] = List()): StreamStage[I, O] = new StreamStage(componentFactory, incoming)
}