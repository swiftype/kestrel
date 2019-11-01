#!/bin/bash
echo "Starting kestrel in development mode..."

# find jar no matter what the root dir name
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  cd "$(dirname "$SOURCE")"
  SOURCE="$(readlink "$SOURCE")"
done
ROOT_DIR="$(cd -P "$(dirname "$SOURCE")"/.. && pwd)"

java -server -Xmx1024m -Dstage=swiftype-development -jar "$ROOT_DIR"/@DIST_NAME@-@VERSION@.jar
