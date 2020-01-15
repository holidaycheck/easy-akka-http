import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import com.holidaycheck.akka.http.{CircuitBreakerConfig, RichClient}
import io.circe.generic.auto._
import io.opencensus.trace.BlankSpan

import scala.concurrent.Future
import scala.concurrent.duration._

object Test extends App {
  implicit val sys: ActorSystem = ActorSystem()

  case class Result(id: String, name: String)

  val client = RichClient(
    identifier = "myendpoint",
    withMetrics = true,
    circuitBreakerConfig = Some(CircuitBreakerConfig(5, 2.second, 30.seconds))
  )

  val uri                    = "https://www.holidaycheck.de/svc/api-destination/v3/destination/7de062f4-676c-3e2b-ad4a-12fd69afbeb6"
  val result: Future[Result] = client.callTo[Result](HttpRequest(uri = uri))
  val resultWithTracing: Future[Result] =
    client.callToTraced[Result](HttpRequest(uri = uri), parentSpan = BlankSpan.INSTANCE)
}
