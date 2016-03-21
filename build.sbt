name := "repox"

organization := "com.gtan"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.2"

libraryDependencies ++= {
  val undertowVer = "1.3.18.Final"
  val logbackVer = "1.1.6"
  val leveldbVer = "0.7"
  val leveldbjniVer = "1.8"
  val scalaTestVer = "2.2.6"
  val playJsonVer = "2.5.0"
  val scalaLoggingVer = "3.1.0"
  val ningVer = "1.9.33"
  val protobufVer = "2.6.1"
  val guavaVer = "19.0"
  Seq(
    "io.undertow" % "undertow-core" % undertowVer,
    ("com.ning" % "async-http-client" % ningVer)
      .exclude("org.slf4j", "slf4j-api"),
    ("com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVer)
      .exclude("org.scala-lang", "scala-library")
      .exclude("org.scala-lang", "scala-reflect"),
    ("ch.qos.logback" % "logback-classic" % logbackVer)
      .exclude("org.slf4j", "slf4j-api"),
    ("com.typesafe.akka" %% "akka-actor" % akkaVersion)
      .exclude("org.slf4j", "slf4j-api"),
    ("com.typesafe.akka" %% "akka-slf4j" % akkaVersion)
      .exclude("org.slf4j", "slf4j-api"),
    ("com.typesafe.akka" %% "akka-agent" % akkaVersion)
      .exclude("org.scala-lang", "scala-library"),
    ("com.typesafe.akka" %% "akka-persistence" % akkaVersion)
      .exclude("org.scala-lang", "scala-library"),
    ("org.iq80.leveldb" % "leveldb" % leveldbVer)
      .exclude("com.google.guava", "guava"),
    "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbjniVer,
    "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion,
    ("com.typesafe.play" %% "play-json" % playJsonVer)
      .exclude("org.scala-lang", "scala-library"),
    "com.google.protobuf" % "protobuf-java" % protobufVer,
    "com.google.guava" % "guava" % guavaVer,
    "org.scalatest" %% "scalatest" % scalaTestVer % "test"
  )
}

transitiveClassifiers := Seq("sources")

scalacOptions ++= Seq(
                       "-feature",
                       "-deprecation",
                       "-language:implicitConversions",
                       //  "-language:higherKinds",
                       //  "-language:existentials",
                       "-language:postfixOps"
                     )

fork := true

assemblyMergeStrategy in assembly := {
  case str@PathList("admin", "bower_components", remains@_*) => remains match {
    case Seq("angular", "angular.min.js") => MergeStrategy.deduplicate
    case Seq("angular-route", "angular-route.min.js") => MergeStrategy.deduplicate
    case Seq("ng-file-upload", "ng-file-upload.min.js") => MergeStrategy.deduplicate
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


mainClass in Compile := Some("com.gtan.repox.Main")

/* package settings */

enablePlugins(JavaServerAppPackaging)

rpmLicense := Some("MIT")


mappings in Universal := {
  val universalMappings = (mappings in Universal).value
  val fatJar = (assembly in Compile).value
  val filtered = universalMappings filter {
    case (file, name) =>  ! name.endsWith(".jar")
  }
  filtered :+ (fatJar -> s"lib/${fatJar.getName}")
}

scriptClasspath := Seq( (jarName in assembly).value )

javaOptions in Universal ++= Seq(
  s"-Dconfig.file=/usr/share/${packageName.value}/conf/appliction.conf",
  s"-Dlogger.file=/usr/share/${packageName.value}/conf/logback.xml"
)

rpmVendor := organization.value

//rpm doesnt like snapshot as version number
version in Rpm := version.value.replace("-SNAPSHOT", "")

linuxPackageMappings += packageTemplateMapping(s"/var/lib/${packageName.value}/storage")().withUser(name.value).withGroup(name.value)

maintainer := name.value
