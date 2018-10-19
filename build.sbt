import Dependencies._
import CompilerOptions._

name in ThisBuild := "easy-akka-http"
organization in ThisBuild := "com.holidaycheck"
scalaVersion in ThisBuild := "2.12.6"
scalacOptions in ThisBuild ++= compilerOptions
scalafmtOnCompile in ThisBuild := true

releasePublishArtifactsAction in ThisBuild := PgpKeys.publishSigned.value
publishTo in ThisBuild := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

lazy val root = (project in file("."))
  .settings(
    publishArtifact := false,
  )
  .aggregate(easyAkkaClient)

lazy val easyAkkaClient = (project in file("easy-akka-client"))
  .settings(
    name := "easy-akka-client",
    libraryDependencies := akkaHttpDependencies ++ cats ++ scalaTest ++ akkaHttpCirce ++ circe
  )
