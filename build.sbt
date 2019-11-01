com.twitter.sbt.StandardProject.newSettings

com.twitter.scrooge.ScroogeSBT.newSettings

name := "kestrel"

organization := "net.lag"

version := "2.4.8-SWIFTYPE01"

scalaVersion := "2.9.2"

resolvers := Seq(
  DefaultMavenRepository,
  "twitter" at "https://maven.twttr.com/",
  "vendored" at ("file://" + file("").absolutePath + "/vendor")
)

libraryDependencies ++= Seq(
  "com.twitter" % "ostrich_2.9.2" % "9.1.1-SWIFTYPE01" force()
    exclude("com.twitter", "scala-json")
    exclude("com.twitter", "util-core")
    exclude("com.twitter", "util-eval")
    exclude("com.twitter", "util-logging")
    exclude("com.twitter", "util-jvm"),
  "com.twitter" % "scala-json_2.9.2" % "3.0.1",
  "com.twitter" % "util-core_2.9.2" % "6.4.0",
  "com.twitter" % "util-eval_2.9.2" % "6.4.0",
  "com.twitter" % "util-jvm_2.9.2" % "6.4.0",
  "com.twitter" % "util-logging_2.9.2" % "6.4.0",
  "com.twitter" %% "naggati" % "4.1.0",
  "com.twitter" % "finagle-core_2.9.2" % "6.4.1",
  "com.twitter" % "finagle-ostrich4_2.9.2" % "6.4.1",
  "org.apache.thrift" % "libthrift" % "0.9.2",
  "com.twitter" % "finagle-thrift_2.9.2" % "6.4.1", // override scrooge's version
  "com.twitter" %% "scrooge-runtime" % "3.1.5"
    exclude("com.twitter", "finagle-core_2.9.2")
    exclude("com.twitter", "finagle-thrift_2.9.2")
    exclude("com.twitter", "util-core_2.9.2"),
  "com.twitter.common.zookeeper" % "server-set" % "1.0.16"
    exclude("com.twitter.common", "args"),
  // for tests only:
  "junit" % "junit" % "4.12" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
  "org.jmock" % "jmock" % "2.4.0" % "test",
  "cglib" % "cglib" % "2.1_3" % "test",
  "asm" % "asm" % "1.5.3" % "test",
  "org.objenesis" % "objenesis" % "1.1" % "test",
  "org.hamcrest" % "hamcrest-all" % "1.1" % "test",
  "org.scalatest" % "scalatest_2.9.0" % "1.9.2" % "test"
)

// time-based tests cannot be run in parallel
logBuffered in Test := false

parallelExecution in Test := false

mainClass in Compile := Some("net.lag.kestrel.Kestrel")

publishArtifact in Test := true

packageDistConfigFilesValidationRegex := Some(".*")
