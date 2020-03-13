# Rich akka http client

`RichClient` makes it easy to do http requests with Akka-HTTP, [circe](https://github.com/circe/circe) for json, 
an [akka circuit breaker](https://doc.akka.io/docs/akka/current/common/circuitbreaker.html) for timeouts,
stats (metrics) and distributed tracing via [OpenCensus](https://github.com/census-ecosystem/opencensus-scala).

The stats are recorded as defined in the [opencensus-spec](https://github.com/census-instrumentation/opencensus-specs/blob/master/stats/HTTP.md).
For further instructions on how to setup exporters for tracing and stats check the 
[opencensus-scala documentation](https://github.com/census-ecosystem/opencensus-scala).


## Usage

Dependency: `"com.holidaycheck" %% "rich-akka-client" % "0.2.5"`

```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import com.holidaycheck.akka.http.{CircuitBreakerConfig, RichClient}
import io.circe.generic.auto._
import io.opencensus.trace.BlankSpan

import scala.concurrent.Future
import scala.concurrent.duration._

object Test extends App {
  implicit val sys: ActorSystem       = ActorSystem()

  case class Result(id: String, name: String)
  
  // identifier will be used for log messages and as a tag for the stats
  val client = RichClient(
    identifier = "myendpoint",
    circuitBreakerConfig = Some(CircuitBreakerConfig(5, 1.second, 30.seconds))
  )

  val uri                    = "https://www.holidaycheck.de/svc/api-destination/v3/destination/7de062f4-676c-3e2b-ad4a-12fd69afbeb6"
  val result: Future[Result] = client.callTo[Result](HttpRequest(uri = uri))
  val resultWithTracing: Future[Result] =
    client.callToTraced[Result](HttpRequest(uri = uri), parentSpan = BlankSpan.INSTANCE)
}
```