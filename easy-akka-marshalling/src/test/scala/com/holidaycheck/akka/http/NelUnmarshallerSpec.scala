package com.holidaycheck.akka.http

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.string.Uuid
import org.scalatest.{AsyncFlatSpec, Matchers}

class NelUnmarshallerSpec extends AsyncFlatSpec with Matchers {
  type ID     = String Refined Uuid
  type Number = Int Refined NonNegative

  it should "compile when using a Nel" in {
    """
      | import com.holidaycheck.akka.http.NelUnmarshaller._
      | import akka.http.scaladsl.server.Directive1
      | import akka.http.scaladsl.server.Directives._
      | import cats.data.NonEmptyList
      |
      | val directive: Directive1[NonEmptyList[String]] = parameter('ids.as[NonEmptyList[String]])
    """.stripMargin should compile
  }

  it should "compile when another inner type is used" in {
    """
      | import com.holidaycheck.akka.http.NelUnmarshaller._
      | import akka.http.scaladsl.server.Directive1
      | import akka.http.scaladsl.server.Directives._
      | import cats.data.NonEmptyList
      |
      | val directive: Directive1[NonEmptyList[Double]] = parameter('ids.as[NonEmptyList[Double]])
    """.stripMargin should compile
  }

  it should "unmarshall" in {
    import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
    import cats.data.NonEmptyList
    import com.holidaycheck.akka.http.NelUnmarshaller._
    implicit val sys: ActorSystem  = ActorSystem()
    implicit val mat: Materializer = ActorMaterializer()
    implicitly[FromStringUnmarshaller[NonEmptyList[Double]]]
      .apply("23,24,25.0")
      .map(_ shouldBe NonEmptyList.of(23.0, 24.0, 25.0))
  }

  it should "throw when an empty argument is given" in {
    import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
    import cats.data.NonEmptyList
    import com.holidaycheck.akka.http.NelUnmarshaller._
    implicit val sys: ActorSystem  = ActorSystem()
    implicit val mat: Materializer = ActorMaterializer()
    recoverToSucceededIf[IllegalArgumentException](implicitly[FromStringUnmarshaller[NonEmptyList[Double]]].apply(""))
  }

  it should "throw when a miss-matching type is given" in {
    import akka.http.scaladsl.unmarshalling.FromStringUnmarshaller
    import cats.data.NonEmptyList
    import com.holidaycheck.akka.http.NelUnmarshaller._
    implicit val sys: ActorSystem  = ActorSystem()
    implicit val mat: Materializer = ActorMaterializer()
    recoverToSucceededIf[IllegalArgumentException](
      implicitly[FromStringUnmarshaller[NonEmptyList[Double]]].apply("hey, you")
    )
  }
}
