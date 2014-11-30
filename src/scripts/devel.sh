#!/bin/bash
echo "Starting kestrel in development mode..."

if ! [ -d /var/log/kestrel ] || ! [ -d /var/spool/kestrel ] ; then
    echo "Creating some dirs as root"
    sudo mkdir -p /var/log/kestrel /var/spool/kestrel
    sudo chown $(whoami):  /var/log/kestrel /var/spool/kestrel
fi

echo "logs:   /var/log/kestrel"
echo "queues: /var/spool/kestrel"

# find jar no matter what the root dir name
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  cd "$(dirname "$SOURCE")"
  SOURCE="$(readlink "$SOURCE")"
done
ROOT_DIR="$(cd -P "$(dirname "$SOURCE")"/.. && pwd)"

java -server -Xmx1024m -Dstage=development -jar "$ROOT_DIR"/@DIST_NAME@-@VERSION@.jar
