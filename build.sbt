import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

name := "repox"

version := "0.0.1"

organization := "com.gtan"

scalaVersion := "2.11.4"

val akkaVersion = "2.3.7"

libraryDependencies ++= Seq(
  "io.undertow" % "undertow-core" % "1.1.0.Final" withSources(),
  ("com.ning" % "async-http-client" % "1.8.15")
    .exclude("org.slf4j", "slf4j-api"),
  ("com.typesafe.scala-logging" %% "scala-logging" % "3.1.0")
    .exclude("org.scala-lang", "scala-library"),
  ("ch.qos.logback" % "logback-classic" % "1.1.2")
    .exclude("org.slf4j", "slf4j-api"),
  ("com.typesafe.akka" %% "akka-actor" % akkaVersion withSources())
    .exclude("org.slf4j", "slf4j-api"),
  ("com.typesafe.akka" %% "akka-slf4j" % akkaVersion)
    .exclude("org.slf4j", "slf4j-api"),
  ("com.typesafe.akka" %% "akka-agent" % akkaVersion)
    .exclude("org.scala-lang", "scala-library"),
  ("com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.7")
    .exclude("org.scala-lang", "scala-library"),
  "com.typesafe.play" %% "play-json" % "2.4.0-M1"
)

// to use repox, this option must be false, or when retry updateClassifiers, it will not download srcs
updateOptions := updateOptions.value.withCachedResolution(false)

fork := true

Revolver.settings

net.virtualvoid.sbt.graph.Plugin.graphSettings

assemblyMergeStrategy in assembly := {
  case str@PathList("admin", "bower_components", remains@_*) => remains match {
    case Seq("angular", "angular.js") => MergeStrategy.deduplicate
    case Seq("angular-route", "angular-route.js") => MergeStrategy.deduplicate
    case Seq("underscore", "underscore-min.js") => MergeStrategy.deduplicate
    case Seq("jquery", "dist", "jquery.min.js") => MergeStrategy.deduplicate
    case Seq("semantic-ui", "dist", "semantic.min.css") => MergeStrategy.deduplicate
    case Seq("semantic-ui", "dist", "semantic.min.js") => MergeStrategy.deduplicate
    case Seq("semantic-ui", "dist", "themes", "default", "assets", all@_*) => MergeStrategy.deduplicate
    case _ => MergeStrategy.discard
  }
  case x =>
    (assemblyMergeStrategy in assembly).value.apply(x)
}

scalacOptions ++= Seq(
  "-feature",
  "-language:postfixOps"
)

packageArchetype.java_server

maintainer in Linux := "repox"

rpmVendor in Rpm := "repox"

rpmLicense in Rpm := Some("http://opensource.org/licenses/MIT")

packageSummary in Rpm := "A lightweight fast artificats repository / proxy"

packageDescription in Rpm := "A lightweight fast artificats repository / proxy"
