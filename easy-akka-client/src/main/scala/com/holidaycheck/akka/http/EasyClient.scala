package com.holidaycheck.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.CircuitBreaker
import akka.stream.Materializer
import cats.effect.IO
import com.holidaycheck.akka.http.EasyClient.RequestFailed
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Decoder

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class EasyClient(request: HttpRequest => Future[HttpResponse])(implicit mat: Materializer)
    extends FailFastCirceSupport {
  def callUrl[R: Decoder](url: String): Future[R] =
    callTo(HttpRequest(uri = url))

  def callUrlIO[R: Decoder](url: String): IO[R] =
    callIOTo(HttpRequest(uri = url))

  def callIOTo[R: Decoder](req: HttpRequest): IO[R] = IO.fromFuture(IO.delay(callTo(req)))

  def callTo[R: Decoder](req: HttpRequest): Future[R] =
    request(req)
      .transformWith {
        case Success(HttpResponse(code, _, entity, _)) if code.isSuccess() && code.allowsEntity() =>
          Unmarshal(entity).to[R]
        case Success(failedResponse) =>
          failedResponse.entity.discardBytes()
          Future.failed(
            RequestFailed(
              s"Could not decode response with code: ${failedResponse.status} for request: $req and failed response: $failedResponse",
              failedResponse.status
            )
          )
        case Failure(err) => Future.failed(err)
      }(mat.executionContext)
}

object EasyClient {
  case class RequestFailed(msg: String, code: StatusCode) extends Throwable(msg)

  def apply()(implicit sys: ActorSystem, mat: Materializer): EasyClient =
    new EasyClient(Http().singleRequest(_))

  def withBreaker(implicit sys: ActorSystem, mat: Materializer): EasyClient = {
    val breaker =
      CircuitBreaker(scheduler = sys.scheduler, maxFailures = 10, callTimeout = 5.second, resetTimeout = 30.seconds)
    new EasyClient(req => breaker.withCircuitBreaker(Http().singleRequest(req)))
  }

  def withBreaker(breaker: CircuitBreaker)(implicit sys: ActorSystem, mat: Materializer): EasyClient =
    new EasyClient(req => breaker.withCircuitBreaker(Http().singleRequest(req)))
}
