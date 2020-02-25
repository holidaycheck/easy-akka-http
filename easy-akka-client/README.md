# Easy akka http client

`EasyClient` provides easy http request response flow in combination with [circe](https://github.com/circe/circe)
for decoding and encoding.

## Usage
Dependency: `"com.holidaycheck" %% "easy-akka-client" % "0.2.3"`

```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.pattern.CircuitBreaker
import akka.stream.ActorMaterializer
import cats.effect.IO
import com.holidaycheck.akka.http.EasyClient
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.Future

object Test {
  implicit val sys: ActorSystem       = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  case class Result(res: String)

  val futureResult: Future[Result]       = EasyClient().callTo[Result](HttpRequest())
  val futureResultForUrl: Future[Result] = EasyClient().callUrl[Result]("www.test.de")

  val ioResult: IO[Result] = EasyClient().callIOTo[Result](HttpRequest())
  val ioResult: IO[Result] = EasyClient().callUrlIO()[Result]("www.test.de")

  // add a circuit breaker
  val futureResult: Future[Result] = EasyClient.withBreaker.callTo[Result](HttpRequest())
  // add a and configure a circuit breaker
  val breaker: CircuitBreaker      = ???
  val futureResult: Future[Result] = EasyClient.withBreaker(breaker).callTo[Result](HttpRequest())
}
```