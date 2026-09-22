#!/bin/sh
set -eu
cd -- "$(dirname -- "$0")"
if [ -n "${JAVA_HOME:-}" ]; then JAVA="$JAVA_HOME/bin/java"; else JAVA=java; fi
if [ ! -f target/ash-viewer.jar ]; then
  echo "Build first: mvn clean verify" >&2
  exit 1
fi
exec "$JAVA" -Xmx768m -jar target/ash-viewer.jar "$@"
