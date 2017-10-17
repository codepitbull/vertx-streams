package io.vertx.lang.scala.streams

import io.reactivex.Flowable
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, AsyncFlatSpec, Matchers}

import scala.concurrent.Future

/**
  * @author <a href="mailto:jochen.mader@codecentric.de">Jochen Mader</a>
  */
@RunWith(classOf[JUnitRunner])
class RxJava2IntegrationTest extends AsyncFlatSpec with Matchers with Assertions {

  "Using Vert.x-streams" should "as Publishers in RxJava2 " ignore {
    Flowable.just("Hello world").subscribe(s => println(s))
    Future.successful(null)
  }
}
