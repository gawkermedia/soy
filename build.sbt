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

// Publishing

lazy val secRing: String = System.getProperty("SEC_RING", "")
lazy val pubRing: String = System.getProperty("PUB_RING", "")
lazy val pgpPass: String = System.getProperty("PGP_PASS", "")

credentials += Credentials(Path.userHome / ".ivy2" / ".sonatype")
pgpSecretRing := file(secRing)
pgpPublicRing := file(pubRing)
pgpPassphrase := Some(Array(pgpPass: _*))

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
  <developers>
    <developer>
      <name>Kinja Developers</name>
      <organization>Gawker Media Group</organization>
      <organizationUrl>http://kinja.com</organizationUrl>
    </developer>
  </developers>
}
