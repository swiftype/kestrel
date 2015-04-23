#!/bin/sh
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  cd "$(dirname "$SOURCE")"
  SOURCE="$(readlink "$SOURCE")"
done
DIST_HOME="$(cd -P "$(dirname "$SOURCE")"/.. && pwd)"
java -server -classpath @DIST_CLASSPATH@ net.lag.kestrel.tools.QDumper "$@"
