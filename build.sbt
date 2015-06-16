// Project settings

name := "soy"

organization := "com.kinja"

// We use Semantic Versioning. See: http://semver.org/
version := "1.0.1"

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

credentials += Credentials(Path.userHome / ".ivy2" / ".sonatype")

pgpSecretRing := file(System.getProperty("SEC_RING", ""))

pgpPublicRing := file(System.getProperty("PUB_RING", ""))

pgpPassphrase := Some(Array(System.getProperty("PGP_PASS", ""): _*))

pomExtra := {
  <url>https://github.com/gawkermedia/soy</url>
  <licenses>
    <license>
      <name>BSD 3-Clause</name>
      <url>https://github.com/gawkermedia/soy/blob/master/LICENSE</url>
    </license>
  </licenses>
  <scm>
    <connection>git@github.com:gawkermedia/soy.git</connection>
    <developerConnection>scm:git:git@github.com:gawkermedia/soy.git</developerConnection>
    <url>git@github.com:gawkermedia/soy</url>
  </scm>
}

// External plugins

com.typesafe.sbt.SbtScalariform.scalariformSettings
