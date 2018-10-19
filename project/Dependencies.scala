import sbt._

object Dependencies {

  val ScalaTestVersion  = "3.0.5"
  val AkkaHttpVersion   = "10.1.3"
  val CatsEffectVersion = "1.0.0"

  lazy val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
  )

  lazy val cats = Seq(
    "org.typelevel" %% "cats-effect" % CatsEffectVersion
  )

  lazy val akkaHttpDependencies = Seq(
    "com.typesafe.akka" %% "akka-http"         % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test
  )
}
