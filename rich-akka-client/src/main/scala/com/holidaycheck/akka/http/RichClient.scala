package com.holidaycheck.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.pattern.CircuitBreaker
import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import io.opencensus.scala.akka.http.{StatsClient, TracingClient}
import io.opencensus.trace.Span

import scala.concurrent.Future
import scala.concurrent.duration._

case class CircuitBreakerConfig(maxFailures: Int, callTimeout: FiniteDuration, resetTimeout: FiniteDuration)

class RichClient(
    doRequest: HttpRequest => Future[HttpResponse],
    identifier: String,
    circuitBreakerConfig: Option[CircuitBreakerConfig]
)(implicit system: ActorSystem, mat: Materializer)
    extends LazyLogging {

  import system.dispatcher

  private val doRequestWithStats = StatsClient.recorded(doRequest, identifier)

  private val circuitBreaker = circuitBreakerConfig.map(c =>
    CircuitBreaker(system.scheduler, c.maxFailures, callTimeout = c.callTimeout, resetTimeout = c.resetTimeout)
      .onCallBreakerOpen(logger.error(s"Circuit Breaker for $identifier opened. Failing fast for ${c.resetTimeout}"))
      .onCallTimeout(_ => logger.error(s"Call to $identifier timed out after ${c.callTimeout}"))
  )

  private def withCB[T](body: => Future[T]): Future[T] = circuitBreaker match {
    case Some(cb) => cb.withCircuitBreaker(body)
    case None     => body
  }

  private def call[R: Decoder](r: HttpRequest => Future[HttpResponse], req: HttpRequest) =
    withCB(EasyClient.callTo(r)(req))

  def callToTraced[R: Decoder](req: HttpRequest, parentSpan: Span): Future[R] =
    call(TracingClient.traceRequest(doRequestWithStats, parentSpan), req)

  def callTo[R: Decoder](req: HttpRequest): Future[R] = call(doRequestWithStats, req)
}

object RichClient {
  def apply(
      doRequest: HttpRequest => Future[HttpResponse],
      identifier: String,
      circuitBreakerConfig: Option[CircuitBreakerConfig]
  )(implicit system: ActorSystem, mat: Materializer) =
    new RichClient(doRequest, identifier, circuitBreakerConfig)

  def apply(
      identifier: String,
      circuitBreakerConfig: Option[CircuitBreakerConfig]
  )(implicit system: ActorSystem, mat: Materializer) =
    new RichClient(Http().singleRequest(_), identifier, circuitBreakerConfig)
}
