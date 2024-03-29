


ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.1"

lazy val root = (project in file("."))
  .settings(
    name := "basic-warhammer40k"
  )

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "cask" % "0.9.2",
  "com.github.cb372" %% "scalacache-caffeine" % "0.28.0",
  "com.github.cb372" %% "scalacache-core" % "0.28.0",
  "org.json4s" %% "json4s-jackson" % "3.6.7",
  "com.lihaoyi" %% "utest" % "0.8.2" % "test",
  "com.lihaoyi" %% "requests" % "0.8.0"
)

testFrameworks +=
  new TestFramework("utest.runner.Framework")

// Docker Plugin Configuration
enablePlugins(
  JavaAppPackaging,
  DockerPlugin
)
Compile / mainClass := Some("warhammer.Main")
Docker / packageName := "warhammer"
////dockerEnvVars ++= Map(("COCKROACH_HOST", "dev.localhost"), ("COCKROACH_PORT", "26257"))
////dockerExposedVolumes := Seq("/opt/docker/.logs", "/opt/docker/.keys")
//
//
//// Define Docker settings
dockerBaseImage := "openjdk:17-jdk-slim" // Base Docker image to use
dockerExposedPorts := Seq(8080) // Ports to expose in the Docker container
dockerUpdateLatest := true // Whether to update the 'latest' tag when publishing
