Build Kestrel
=============

Use sbt 0.11.2
--------------

Kestrel is deprecated project and using old sbt 0.11.2.
To use this version, install sbt from homebrew and add `project/build.properties`.

    $ brew install sbt
    $ cat > project/build.properties
    sbt.version=0.11.2

Create IntelliJ Project
-----------------------

Use [sbt-idea](https://github.com/mpeltonen/sbt-idea/tree/sbt-0.11.2) for sbt-0.11.2 to generate IntelliJ project.
Add next line to `project/plugins.sbt`.

    $ cat >> project/plugins.sbt
    addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")

Then run next

    $ sbt gen-idea

Update deprecated repository
----------------------------

*This change may be not needed.*

Repository "scala-tools.org" is deprecated. Need to add these lines to `project/Build.scala`.

    resolvers ++= Seq(
      "scala-tools" at "https://oss.sonatype.org/content/groups/scala-tools/"
    ),

Test fails
----------

One of 2.4.0 test, `ThriftHandlerSpec.scala` fail to mock `InetSockAddress` and all tests in that file fail.
Apply next patch to fix it.

    --- a/src/test/scala/net/lag/kestrel/ThriftHandlerSpec.scala
    +++ b/src/test/scala/net/lag/kestrel/ThriftHandlerSpec.scala
    @@ -35,7 +35,7 @@ class ThriftHandlerSpec extends Specification with JMocker with ClassMocker {
      "ThriftHandler" should {
        val queueCollection = mock[QueueCollection]
        val connection = mock[ClientConnection]
    -    val address = mock[InetSocketAddress]
    +    val address = new InetSocketAddress(10000)
        val timer = new MockTimer()

Running it in debug mode
------------------------

You may not want to write something in `/var` while the development.
Apply next patch to use current directory.

    --- a/config/development.scala
    +++ b/config/development.scala
    @@ -10,7 +10,9 @@ new KestrelConfig {
      textListenPort = 2222
      thriftListenPort = 2229
    
    -  queuePath = "/var/spool/kestrel"
    +  val currentPath = new java.io.File(".").getAbsolutePath
    +
    +  queuePath = currentPath + "/var/spool"
    
      clientTimeout = 30.seconds
    
    @@ -76,7 +78,7 @@ new KestrelConfig {
      loggers = new LoggerConfig {
        level = Level.INFO
        handlers = new FileHandlerConfig {
    -      filename = "/var/log/kestrel/kestrel.log"
    +      filename = currentPath + "/var/kestrel.log"
          roll = Policy.Never
        }
      }
