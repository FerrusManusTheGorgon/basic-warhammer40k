ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.1"

lazy val root = (project in file("."))
  .settings(
    name := "basic-warhammer40k"
  )

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "cask" % "0.9.2",
//  "com.typesafe.play" %% "play-json" % "2.10.0",
  "com.github.cb372" %% "scalacache-caffeine" % "0.28.0",
  "com.github.cb372" %% "scalacache-core" % "0.28.0",
//  "com.github.cb372" %% "scalacache-cats-effect" % "0.28.0"

//  "com.github.cb372" %% "scalacache-caffeine" % "1.0.0-M6",
//  "com.github.cb372" %% "scalacache-core" % "1.0.0-M6",
)
