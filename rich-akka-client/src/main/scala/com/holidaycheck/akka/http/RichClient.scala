package com.holidaycheck.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.pattern.CircuitBreaker
import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import io.opencensus.scala.akka.http.TracingClient
import io.opencensus.trace.Span
import io.prometheus.client.Histogram

import scala.concurrent.Future
import scala.concurrent.duration._

case class CircuitBreakerConfig(maxFailures: Int, callTimeout: FiniteDuration, resetTimeout: FiniteDuration)

class RichClient(
    doRequest: HttpRequest => Future[HttpResponse],
    identifier: String,
    withMetrics: Boolean,
    circuitBreakerConfig: Option[CircuitBreakerConfig]
)(implicit system: ActorSystem, mat: Materializer)
    extends LazyLogging {

  import system.dispatcher

  private def withCB[T](body: => Future[T]): Future[T] = circuitBreakerConfig match {
    case Some(c) =>
      CircuitBreaker(system.scheduler, c.maxFailures, callTimeout = c.callTimeout, resetTimeout = c.resetTimeout)
        .onCallBreakerOpen(logger.error(s"Circuit Breaker for $identifier opened. Failing fast for ${c.resetTimeout}"))
        .onCallTimeout(_ => logger.error(s"Call to $identifier timed out after ${c.callTimeout}"))
        .withCircuitBreaker(body)
    case None => body
  }

  private val histogram = if (withMetrics) Some(RichClient.requestDurationMetric) else None

  private def measureTiming[T](f: => Future[T]): Future[T] = {
    histogram match {
      case Some(h) =>
        val timer  = h.labels(identifier).startTimer()
        val result = f
        result.onComplete(_ => timer.close())
        result
      case None =>
        f
    }
  }

  private def call[R: Decoder](r: HttpRequest => Future[HttpResponse], req: HttpRequest) =
    withCB(measureTiming(EasyClient.callTo(r)(req)))

  def callToTraced[R: Decoder](req: HttpRequest, parentSpan: Span): Future[R] =
    call(TracingClient.traceRequest(doRequest, parentSpan), req)

  def callTo[R: Decoder](req: HttpRequest): Future[R] = call(doRequest, req)
}

object RichClient {

  private[RichClient] val requestDurationMetric =
    Histogram
      .build()
      .name(s"http_client_request_duration_seconds")
      .help(s"Duration in seconds until the whole body was received & parsed")
      .buckets(0.01, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5, 0.75, 1.0, 1.5, 2.5, 5.0)
      .labelNames("target")
      .register()

  def apply(
      doRequest: HttpRequest => Future[HttpResponse],
      identifier: String,
      withMetrics: Boolean,
      circuitBreakerConfig: Option[CircuitBreakerConfig]
  )(implicit system: ActorSystem, mat: Materializer) =
    new RichClient(doRequest, identifier, withMetrics, circuitBreakerConfig)

  def apply(
      identifier: String,
      withMetrics: Boolean,
      circuitBreakerConfig: Option[CircuitBreakerConfig]
  )(implicit system: ActorSystem, mat: Materializer) =
    new RichClient(Http().singleRequest(_), identifier, withMetrics, circuitBreakerConfig)
}
