import sbt._

object Dependencies {
  val ScalaTestVersion     = "3.0.5"
  val AkkaHttpVersion      = "10.1.3"
  val CatsEffectVersion    = "1.0.0"
  val AkkaVersion          = "2.5.17"
  val CirceVersion         = "0.10.0"
  val AkkaHttpCirceVersion = "1.22.0"

  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-refined",
    "io.circe" %% "circe-parser"
  ).map(_ % CirceVersion)

  lazy val akkaHttpCirce = Seq(
    "de.heikoseeberger" %% "akka-http-circe" % AkkaHttpCirceVersion
  )

  lazy val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
  )

  lazy val cats = Seq(
    "org.typelevel" %% "cats-effect" % CatsEffectVersion
  )

  lazy val akkaHttpDependencies = Seq(
    "com.typesafe.akka" %% "akka-http"         % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream"       % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test
  )
}
