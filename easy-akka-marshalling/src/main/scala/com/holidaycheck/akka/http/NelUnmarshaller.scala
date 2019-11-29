package com.holidaycheck.akka.http

import akka.http.scaladsl.unmarshalling.Unmarshaller.CsvSeq
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import cats.data.NonEmptyList
import cats.syntax.list._

object NelUnmarshaller {
  implicit def nelUnmarshaller[A: FromStringUnmarshaller]: Unmarshaller[String, NonEmptyList[A]] =
    Unmarshaller
      .strict[String, String](s =>
        if (s.nonEmpty) s
        else throw new IllegalArgumentException(s"$s needs to be non-empty")
      )
      .andThen(
        CsvSeq[A]
          .map(seq => seq.toList.toNel.getOrElse(throw new IllegalArgumentException(s"$seq is not an non-empty list")))
      )
}
