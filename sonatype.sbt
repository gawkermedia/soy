import xerial.sbt.Sonatype._

sonatypeProfileName := "com.kinja"

publishMavenStyle := true
// useGpg := true

description := "Scala data structures for Google Closure Templates"
homepage := Some(url("https://github.com/gawkermedia/soy"))
licenses := Seq("BSD 3-Clause" -> url("https://github.com/gawkermedia/soy/blob/master/LICENSE"))

sonatypeProjectHosting := Some(GitHubHosting("gawkermedia", "soy", ""))

developers := List(
  Developer(id = "GawkerMedia", name = "Kinja Developers", email = "", url = url("http://kinja.com"))
)

credentials += Credentials(Path.userHome / ".ivy2" / ".sonatype")
pgpSecretRing := file(System.getProperty("SEC_RING", ""))
pgpPublicRing := file(System.getProperty("PUB_RING", ""))
pgpPassphrase := Some(Array(System.getProperty("PGP_PASS", ""): _*))
