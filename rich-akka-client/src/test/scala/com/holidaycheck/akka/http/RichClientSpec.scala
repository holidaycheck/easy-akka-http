package com.holidaycheck.akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.Future

class RichClientSpec extends AnyFlatSpec with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem()

  it should "be possible to create two instances with metrics enabled" in {
    def createClient(identifier: String) =
      RichClient(_ => Future.successful(HttpResponse()), identifier, withMetrics = true, None)

    createClient("client1")
    createClient("client2")
  }

  override def afterAll(): Unit = {
    system.terminate()
    super.afterAll()
  }

}
