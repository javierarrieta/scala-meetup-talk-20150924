enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(GitVersioning)

name := """document-processing"""
organization := """com.nitro.platform"""
maintainer in Docker := "Nitro Platform team"
git.useGitDescribe := true

dockerRepository in Docker := Some("home.arrieta.nom.es/nitro")
dockerUpdateLatest in Docker := true

//No Kafka streaming available for scala 2.11
scalaVersion := "2.10.5"

val sparkVersion = "1.3.0"

resolvers ++= Seq("Confluentic repository" at "http://packages.confluent.io/maven/")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % Test,
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka" % sparkVersion,
  "org.apache.kafka" %% "kafka" % "0.8.2.1",
  "org.apache.pdfbox" % "pdfbox" % "1.8.9",
  "com.typesafe" % "config" % "1.2.1",
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.5.2",
  "com.gilt" %% "gfc-avro" % "0.1.1",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "io.confluent" % "kafka-avro-serializer" % "1.0" exclude ("org.slf4j", "slf4j-log4j12") exclude("log4j", "log4j"),
  "com.nitro.platform" %% "avro-codegen-runtime" % "0.0.1-SNAPSHOT"
)

scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation","-feature", "-language:postfixOps")
