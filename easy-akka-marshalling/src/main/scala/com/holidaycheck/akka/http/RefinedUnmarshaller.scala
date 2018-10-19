package com.holidaycheck.akka.http

import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.refineV

object RefinedUnmarshaller {
  implicit def refinedFromStringUnmarshaller[Predicate, RefinedBaseType](
      implicit validate: Validate[RefinedBaseType, Predicate],
      fromStringUnm: FromStringUnmarshaller[RefinedBaseType]
  ): FromStringUnmarshaller[RefinedBaseType Refined Predicate] =
    fromStringUnm.andThen(
      Unmarshaller.strict[RefinedBaseType, RefinedBaseType Refined Predicate](
        in => refineV[Predicate](in).fold(err => throw new Exception(s"$in is not valid: $err"), identity)
      )
    )
}
