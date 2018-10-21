package com.holidaycheck.akka.http

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.string.Uuid
import org.scalatest.{FlatSpec, Matchers}

class RefinedUnmarshallerSpec extends FlatSpec with Matchers {
  type ID     = String Refined Uuid
  type Number = Int Refined NonNegative

  it should "compile when using a refined type for extracting parameters" in {
    """
      | import com.holidaycheck.akka.http.RefinedUnmarshaller._
      | import akka.http.scaladsl.server.Directive1
      | import akka.http.scaladsl.server.Directives._
      | val directive: Directive1[Refined[String, Uuid]] = parameter('id.as[ID])
    """.stripMargin should compile
  }

  it should "compile when using a refined base type other than string" in {
    """
      | import com.holidaycheck.akka.http.RefinedUnmarshaller._
      | import akka.http.scaladsl.server.Directive1
      | import akka.http.scaladsl.server.Directives._
      | val directive: Directive1[Refined[Int, NonNegative]] = parameter('id.as[Number])
    """.stripMargin should compile
  }

  it should "NOT compile when missing the RefinedUnmarshaller import" in {
    """
      | import akka.http.scaladsl.server.Directive1
      | import akka.http.scaladsl.server.Directives._
      | val directive: Directive1[Refined[String, Uuid]] = parameter('id.as[ID])
    """.stripMargin shouldNot compile
  }
}
