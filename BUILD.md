Build Kestrel
=============

Use sbt
-------

Use sbt to build kestrel.

    $ brew install sbt
    $ export JAVA_OPTS='-Dhttps.protocols=TLSv1.1,TLSv1.2'
    $ sbt package-dist

Create IntelliJ Project
-----------------------

Use latest IntelliJ which supprots sbt project.
Simply import project directory as sbt project then it works.

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
