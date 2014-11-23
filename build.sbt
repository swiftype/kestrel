com.twitter.scrooge.ScroogeSBT.newSettings

name := "kestrel"

organization := "net.lag"

version := "2.4.2-SNAPSHOT"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
  "com.twitter" % "ostrich" % "8.2.9",
  "com.twitter" %% "naggati" % "4.1.0",
  "com.twitter" % "finagle-core" % "5.3.19",
  "com.twitter" % "finagle-ostrich4" % "5.3.19",
  "com.twitter" % "finagle-thrift" % "5.3.19",
  "com.twitter" %% "scrooge-runtime" % "3.1.5"
    exclude("com.twitter", "finagle-core")
    exclude("com.twitter", "finagle-thrift"),
  "com.twitter.common.zookeeper" % "server-set" % "1.0.16",
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test",
  "org.jmock" % "jmock" % "2.4.0" % "test",
  "cglib" % "cglib" % "2.1_3" % "test",
  "asm" % "asm" % "1.5.3" % "test",
  "org.objenesis" % "objenesis" % "1.1" % "test",
  "org.hamcrest" % "hamcrest-all" % "1.1" % "test"
)

logBuffered in Test := false

parallelExecution in Test := false

mainClass in Compile := Some("net.lag.kestrel.Kestrel")

publishArtifact in Test := true
