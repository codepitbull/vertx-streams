package io.vertx.lang.scala.streams.reactivestreams

import io.vertx.lang.scala.VertxExecutionContext
import org.reactivestreams.tck.{PublisherVerification, TestEnvironment}
import org.reactivestreams.Publisher
import org.scalatest.{Assertions, Matchers}
import io.vertx.lang.scala.streams.source.VertxListSource
import io.vertx.scala.core.Vertx
import io.vertx.lang.scala.streams.Stream._

class TckSpec(testEnvironment: TestEnvironment, gcTimeOut: Long) extends PublisherVerification[String](testEnvironment, gcTimeOut) with Matchers with Assertions  {

  def this() = this(new TestEnvironment(300l), 1000L)

  override def createPublisher(amount: Long): Publisher[String] = {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = new VertxExecutionContext(ctx)
    val listSource =  if(amount < Long.MaxValue)
                        new VertxListSource[String]((1 to amount.toInt).toList.map(i => i.toString))
                      else
                        new VertxListSource[String](List())
    listSource.stream.publisher()
  }

  override def createFailedPublisher(): Publisher[String] = {
    null
  }
}
