// Project settings

name := "soy"

organization := "com.kinja"

version := "0.3.3" + {if (System.getProperty("JENKINS_BUILD") == null) "-SNAPSHOT" else ""}

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

javacOptions ++= Seq("-Xlint:deprecation")

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

// Dependencies

libraryDependencies ++= Seq(
	"com.google.template" % "soy-excluding-deps" % "2015-01-09",
	"com.google.guava" % "guava" % "17.0" % "test",
	"org.specs2" %% "specs2-core" % "2.4.15" % "test",
	"org.specs2" %% "specs2-mock" % "2.4.15" % "test",
	"org.specs2" %% "specs2-junit" % "2.4.15" % "test"
)

// Publishing

resolvers += "Gawker Public Group" at "https://nexus.kinja-ops.com/nexus/content/groups/public/"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo <<= (version)(version =>
  if (version endsWith "SNAPSHOT") Some("Gawker Snapshots" at "https://nexus.kinja-ops.com/nexus/content/repositories/snapshots/")
  else                             Some("Gawker Releases" at "https://nexus.kinja-ops.com/nexus/content/repositories/releases/")
)

// External plugins

com.typesafe.sbt.SbtScalariform.scalariformSettings
