package io.vertx.lang.scala.reactivestreams

import org.junit.Ignore
import org.reactivestreams.Publisher
import org.reactivestreams.tck.{PublisherVerification, TestEnvironment}
import org.scalatest.{Assertions, Matchers}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class FuturePublisherTCKTest(testEnvironment: TestEnvironment, gcTimeOut: Long) extends PublisherVerification[String](testEnvironment, gcTimeOut) with Matchers with Assertions  {

  def this() = this(new TestEnvironment(300l), 1000L)

  override def createPublisher(amount: Long): Publisher[String] = {
    new FuturePublisher[String](Future.successful("Hallo Welt"))
  }

  override def createFailedPublisher(): Publisher[String] = {
    null
  }

  //The following tests are ignored as they don't make sense for a singleshot Publisher

  @Ignore
  override def required_createPublisher3MustProduceAStreamOfExactly3Elements(): Unit = {}

  @Ignore
  override def required_spec101_subscriptionRequestMustResultInTheCorrectNumberOfProducedElements(): Unit = {}

  @Ignore
  override def required_spec102_maySignalLessThanRequestedAndTerminateSubscription(): Unit = {}

  @Ignore
  override def required_spec105_mustSignalOnCompleteWhenFiniteStreamTerminates(): Unit = {}

  @Ignore
  override def required_spec107_mustNotEmitFurtherSignalsOnceOnCompleteHasBeenSignalled(): Unit = {}

  @Ignore
  override def required_spec317_mustSupportACumulativePendingElementCountUpToLongMaxValue(): Unit = {}

  @Ignore
  override def required_spec317_mustSupportAPendingElementCountUpToLongMaxValue(): Unit = {}
}
