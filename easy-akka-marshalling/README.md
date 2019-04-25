# Easy akka http marshalling

## Using final tagless in akka-http servers - FMarshaller

If you want to not restrict your akka http server to run only with Scalas `Futures`
this modules can help you with it

This code does not compile, as it does not know how to create a `ToResponseMarshaller` for the
`F[_]`.
```scala
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

case class Response(text: String)
class Endpoint[F[_]](getResult: () => F[Response]) {

  val route: Route = (get & path("test")) {
    complete(getResult())
  }
}

```

We just need to add `import com.holidaycheck.akka.http.FMarshaller._` and the `FMarshaller[F]`

```scala
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import com.holidaycheck.akka.http.FMarshaller._
import com.holidaycheck.akka.http.FMarshaller

case class Response(text: String)
class Endpoint[F[_]: FMarshaller](getResult: () => F[Response]) {

  val route: Route = (get & path("test")) {
    complete(getResult())
  }
}

```

`FMarshaller`s for `cats.effect.IO`, `cats.Id` and `Future` are included.

For the IO `FMarshaller` a `ContextShift[IO]` has to be provided, to prevent blocking the main thread.

## Using refined types in akka to extract request parameters - RefinedUnmarshaller

This helps integrate [refined types](https://github.com/fthomas/refined) to akka parameter extraction in routes.

E.g.

```scala
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import com.holidaycheck.akka.http.RefinedUnmarshaller._

object Endpoint {
  type Limit = Int Refined Positive

  val route: Route = (get & path("test")) {
    parameter('ip.as[Limit]) { limit =>
      println(limit)
      complete("Thanks, bye")
    }
  }
}
```

Whenever you have some `FromStringUnmarshaller` available for transforming the
parameter String to your refined type's base type (e.g. the `Int` here) and have
this`import com.holidaycheck.akka.http.RefinedUnmarshaller._` import creates an `FromStringUnmarshaller` to your
refined types (e.g. `Limit` here).

## More Unmarshalling and Marshalling

When using `io.circe` for decoding and encoding, just add [circes refined](https://github.com/circe/circe/tree/master/modules/refined) library.
This give you free marshalling together with [akka http circe](https://github.com/hseeberger/akka-http-json/tree/master/akka-http-circe/src).