import scalariform.formatter.preferences._

name := "soy"

organization := "com.kinja"

// We use Semantic Versioning. See: http://semver.org/
version := "4.0.0"

crossScalaVersions := Seq("2.11.12", "2.12.11", "2.13.1")

scalaVersion := crossScalaVersions.value.head

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

javacOptions ++= Seq("-Xlint:deprecation")

// Dependencies

val specs2Version = "4.8.3"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.4",
	"com.google.template" % "soy" % "2016-08-09",
	"org.specs2" %% "specs2-core" % specs2Version % "test",
	"org.specs2" %% "specs2-mock" % specs2Version % "test",
	"org.specs2" %% "specs2-junit" % specs2Version % "test",
	"org.scalatest" %% "scalatest" % "3.1.1" % "test"
)

publishTo := sonatypePublishTo.value
