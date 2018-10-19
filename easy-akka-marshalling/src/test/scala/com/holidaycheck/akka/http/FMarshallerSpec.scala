package com.holidaycheck.akka.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import org.scalatest.{FlatSpec, Matchers}

class FMarshallerSpec extends FlatSpec with Matchers {

  behavior of "Completing akka routes for F"

  def routeWithF[F[_]: FMarshaller](toRes: () => F[String]): StandardRoute = {
    import com.holidaycheck.akka.http.FMarshaller._
    complete(toRes())
  }

  it should "find an implicit to compile for Id" in {
    """
      | import cats.Id
      | routeWithF[Id](() => "OK")
    """.stripMargin should compile
  }

  it should "find an implicit to compile for IO" in {
    """
      | import cats.effect.IO
      | routeWithF[IO](() => IO.pure("OK"))
    """.stripMargin should compile
  }

  it should "find an implicit to compile for Future" in {
    """
      | import scala.concurrent.Future
      | routeWithF[Future](() => Future.successful("OK"))
    """.stripMargin should compile
  }

  it should "Not compile for an unsupported F" in {
    """
      | routeWithF[Option](() => Some("OK"))
    """.stripMargin shouldNot compile
  }

  it should "Not compile when ` com.holidaycheck.akka.http.FMarshaller._` is not imported" in {
    """
      | import cats.effect.IO
      | implicitly[FMarshaller[IO]]
      | complete(IO.pure("Missing import"))
    """.stripMargin shouldNot compile
  }
}
