package io.vertx.lang.scala.streams

import io.vertx.lang.scala.streams.sink.FunctionSink

import scala.collection.mutable
import scala.concurrent.Promise

class TestFunctionSink(expectedListSize:Int) {
  val promise: Promise[List[Int]] = Promise[List[Int]]

  private val streamed: mutable.Buffer[Int] = mutable.Buffer[Int]()

  val sink = new FunctionSink[Int](f => {
    if (streamed.size < expectedListSize)
      streamed += f
    if (streamed.size == expectedListSize)
      promise.success(streamed.toList)
  })
}

object TestFunctionSink {
  def apply(expectedListSize:Int): TestFunctionSink = new TestFunctionSink(expectedListSize)
}