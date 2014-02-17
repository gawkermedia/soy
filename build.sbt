
import com.typesafe.sbt.SbtScalariform._

// Project settings

name := "soy"

organization := "com.kinja"

version := "0.2-" + {if (System.getProperty("JENKINS_BUILD") == null) "SNAPSHOT" else "RELEASE"}

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

javacOptions ++= Seq("-Xlint:deprecation")

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

// Dependencies

libraryDependencies ++= Seq(
	("com.google.template" % "soy" % "2012-12-21").exclude("asm", "asm"),
	"org.specs2" %% "specs2" % "2.3.8" % "test"
)

// Publishing

resolvers += "Gawker Public Group" at "https://nexus.kinja-ops.com/nexus/content/groups/public/"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo <<= (version)(version =>
  if (version endsWith "SNAPSHOT") Some("Gawker Snapshots" at "https://nexus.kinja-ops.com/nexus/content/repositories/snapshots/")
  else                             Some("Gawker Releases" at "https://nexus.kinja-ops.com/nexus/content/repositories/releases/")
)

// External plugins

scalariformSettings
