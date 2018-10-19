package com.holidaycheck.akka.http

import akka.http.scaladsl.marshalling.{GenericMarshallers, ToResponseMarshaller}
import cats.Id
import cats.effect.IO

import scala.concurrent.Future

trait FMarshaller[F[_]] {
  implicit def marshallF[A](implicit m: ToResponseMarshaller[A]): ToResponseMarshaller[F[A]]
}

object FMarshaller {
  implicit def fmarshaller[F[_], A: ToResponseMarshaller](implicit M: FMarshaller[F]): ToResponseMarshaller[F[A]] =
    M.marshallF

  implicit val futureMarshaller: FMarshaller[Future] = new FMarshaller[Future] {
    override implicit def marshallF[A](implicit m: ToResponseMarshaller[A]): ToResponseMarshaller[Future[A]] =
      GenericMarshallers.futureMarshaller
  }

  implicit val ioMarshaller: FMarshaller[IO] = new FMarshaller[IO] {
    override implicit def marshallF[A](implicit m: ToResponseMarshaller[A]): ToResponseMarshaller[IO[A]] =
      GenericMarshallers.futureMarshaller(m).compose(io => io.unsafeToFuture())
  }

  implicit val idMarshaller: FMarshaller[Id] = new FMarshaller[Id] {
    override implicit def marshallF[A](implicit m: ToResponseMarshaller[A]): ToResponseMarshaller[Id[A]] = m
  }
}
