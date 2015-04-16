// Project settings

name := "soy"

organization := "com.kinja"

// We use Semantic Versioning. See: http://semver.org/
version := "0.4.2" + {if (System.getProperty("JENKINS_BUILD") == null) "-SNAPSHOT" else ""}

crossScalaVersions := Seq("2.10.4", "2.11.6")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

javacOptions ++= Seq("-Xlint:deprecation")

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

// Dependencies

libraryDependencies ++= Seq(
	"com.google.template" % "soy-excluding-deps" % "2015-01-09",
	"com.google.guava" % "guava" % "17.0" % "test",
	"org.specs2" %% "specs2-core" % "2.4.15" % "test",
	"org.specs2" %% "specs2-mock" % "2.4.15" % "test",
	"org.specs2" %% "specs2-junit" % "2.4.15" % "test",
	"org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _)

libraryDependencies ++= {
	CrossVersion.partialVersion(scalaVersion.value) match {
		case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq()
		case Some((2, 10)) => Seq(
			"org.scalamacros" %% "quasiquotes" % "2.0.1",
			compilerPlugin("org.scalamacros" % "paradise_2.10.4" % "2.0.1"))
	}
}

// Publishing

resolvers += "Gawker Public Group" at "https://nexus.kinja-ops.com/nexus/content/groups/public/"

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishTo <<= (version)(version =>
  if (version endsWith "SNAPSHOT") Some("Gawker Snapshots" at "https://nexus.kinja-ops.com/nexus/content/repositories/snapshots/")
  else                             Some("Gawker Releases" at "https://nexus.kinja-ops.com/nexus/content/repositories/releases/")
)

// External plugins

com.typesafe.sbt.SbtScalariform.scalariformSettings
