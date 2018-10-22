package com.holidaycheck.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import com.holidaycheck.akka.http.EasyClient.RequestFailed
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import org.scalatest.{AsyncFlatSpec, Matchers}

import scala.concurrent.Future

class EasyClientSpec extends AsyncFlatSpec with Matchers with FailFastCirceSupport {

  implicit val sys: ActorSystem       = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  case class ExampleResponse(name: String)
  val testResponse = ExampleResponse("hello")

  it should "unmarshal the result" in {
    new EasyClient(_ => Marshal(testResponse).to[HttpResponse])
      .callTo[ExampleResponse](HttpRequest())
      .map(_ shouldBe testResponse)
  }

  it should "unmarshal the result for a request with url" in {
    new EasyClient(_ => Marshal(testResponse).to[HttpResponse])
      .callUrl[ExampleResponse]("www.test.de")
      .map(_ shouldBe testResponse)
  }

  it should "unmarshal the result for a request to IO" in {
    new EasyClient(_ => Marshal(testResponse).to[HttpResponse])
      .callIOTo[ExampleResponse](HttpRequest())
      .unsafeRunSync() shouldBe testResponse
  }

  it should "unmarshal the result for a request to IO for url" in {
    new EasyClient(_ => Marshal(testResponse).to[HttpResponse])
      .callUrlIO[ExampleResponse]("www.test.de")
      .unsafeRunSync() shouldBe testResponse
  }

  it should "fail when status is not success" in {
    val failingService = new EasyClient(_ => Future.successful(HttpResponse(status = StatusCodes.EarlyHints)))
      .callTo[ExampleResponse](HttpRequest())
    recoverToSucceededIf[RequestFailed](failingService)
  }

  it should "fail when status does not allow an entity" in {
    val failingService = new EasyClient(_ => Future.successful(HttpResponse(status = StatusCodes.NoContent)))
      .callTo[ExampleResponse](HttpRequest())
    recoverToSucceededIf[RequestFailed](failingService)
  }

  it should "fail when http request fails" in {
    val failingService = new EasyClient(_ => Future.failed(new RuntimeException("TEST")))
      .callTo[ExampleResponse](HttpRequest())
    recoverToSucceededIf[RuntimeException](failingService)
  }
}
