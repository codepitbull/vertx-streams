package io.vertx.lang.scala.streams.reactivestreams

import io.vertx.lang.scala.VertxExecutionContext
import io.vertx.lang.scala.streams.Stream._
import io.vertx.scala.core.Vertx
import org.reactivestreams.tck.{SubscriberBlackboxVerification, TestEnvironment}
import org.scalatest.{Assertions, Matchers}

class WriteStreamSubscriberTCKTest(testEnvironment: TestEnvironment, gcTimeOut: Long) extends SubscriberBlackboxVerification[String](testEnvironment) with Matchers with Assertions  {

  def this() = this(new TestEnvironment(300l), 1000L)

  override def createSubscriber() = {
    val vertx = Vertx.vertx()
    val ctx = vertx.getOrCreateContext()
    implicit val ec = new VertxExecutionContext(ctx)

    vertx.eventBus().localConsumer[String]("uiui").handler(msg => println(msg.body()))
    vertx.eventBus().sender[String]("uiui").subscriber()
  }

  override def createElement(element: Int) = {
    element.toString
  }
}
