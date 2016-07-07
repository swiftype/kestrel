com.twitter.sbt.StandardProject.newSettings

com.twitter.scrooge.ScroogeSBT.newSettings

Rpmbuild.newSettings

name := "kestrel"

organization := "net.lag"

version := "2.4.4-SWIFTYPE08"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "twitter" at "http://maven.twttr.com/",
  "sonatype" at "https://oss.sonatype.org/content/repositories/scala-tools",
  "vendored" at ("file://" + file("").absolutePath + "/vendor")
)


libraryDependencies ++= Seq(
  "com.twitter" % "ostrich" % "8.2.9-SWIFTYPE01" force(),
  "com.twitter" %% "naggati" % "4.1.0",
  "com.twitter" % "finagle-core" % "5.3.19",
  "com.twitter" % "finagle-ostrich4" % "5.3.19",
  "com.twitter" % "finagle-thrift" % "5.3.19",
  "com.twitter" %% "scrooge-runtime" % "3.1.5"
    exclude("com.twitter", "finagle-core_2.9.2")
    exclude("com.twitter", "finagle-thrift_2.9.2")
    exclude("com.twitter", "util-core_2.9.2"),
  "com.twitter.common.zookeeper" % "server-set" % "1.0.16"
    exclude("com.twitter.common", "args"),
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
  "org.jmock" % "jmock" % "2.4.0" % "test",
  "cglib" % "cglib" % "2.1_3" % "test",
  "asm" % "asm" % "1.5.3" % "test",
  "org.objenesis" % "objenesis" % "1.1" % "test",
  "org.hamcrest" % "hamcrest-all" % "1.1" % "test",
  "junit" % "junit" % "4.12" % "test",
  "org.apache.thrift" % "libthrift" % "0.9.2"
)

logBuffered in Test := false

parallelExecution in Test := false

mainClass in Compile := Some("net.lag.kestrel.Kestrel")

publishArtifact in Test := true

packageDistConfigFilesValidationRegex := Some(".*")

rpmbuildRelease := 1
