import sbt._

object Dependencies {
  val ScalaTestVersion     = "3.1.1"
  val AkkaHttpVersion      = "10.1.11"
  val CatsEffectVersion    = "2.1.2"
  val AkkaVersion          = "2.6.2"
  val CirceVersion         = "0.14.0"
  val AkkaHttpCirceVersion = "1.30.0"
  val RefinedVersion       = "0.9.12"

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

  lazy val refined = Seq(
    "eu.timepit" %% "refined"            % RefinedVersion,
    "eu.timepit" %% "refined-pureconfig" % RefinedVersion
  )

  lazy val akkaHttpDependencies = Seq(
    "com.typesafe.akka" %% "akka-http"           % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream"         % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit"   % AkkaHttpVersion % Test
  )

  lazy val opencensus = Seq(
    "com.github.sebruck" %% "opencensus-scala-akka-http" % "0.7.2"
  )
}
