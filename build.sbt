import Dependencies._

name in ThisBuild := "easy-akka-http"
organization in ThisBuild := "com.holidaycheck"
scalaVersion in ThisBuild := "2.13.1"
crossScalaVersions in ThisBuild := Seq("2.13.1", "2.12.19")
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
    publishArtifact := false
  )
  .aggregate(easyAkkaClient, easyAkkaMarshalling, richAkkaClient)

lazy val easyAkkaClient = (project in file("easy-akka-client"))
  .settings(
    name := "easy-akka-client",
    libraryDependencies := akkaHttpDependencies ++ cats ++ scalaTest ++ akkaHttpCirce ++ circe
  )

lazy val richAkkaClient = (project in file("rich-akka-client"))
  .settings(
    name := "rich-akka-client",
    libraryDependencies := opencensus ++ scalaTest
  )
  .dependsOn(easyAkkaClient)

lazy val easyAkkaMarshalling = (project in file("easy-akka-marshalling"))
  .settings(
    name := "easy-akka-marshalling",
    libraryDependencies := akkaHttpDependencies ++ cats ++ scalaTest ++ akkaHttpCirce ++ circe ++ refined
  )
