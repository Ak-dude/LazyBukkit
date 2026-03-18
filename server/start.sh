#!/usr/bin/env bash
# Optional convenience wrapper. You can also just run:
#   java -Xmx2G -jar target/lazybukkit.jar nogui
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT="$(dirname "$DIR")"
JAR="$PROJECT/target/lazybukkit.jar"
RAM="${1:-2G}"

if [ ! -f "$JAR" ]; then
    echo "lazybukkit.jar not found. Building..."
    (cd "$PROJECT" && bash build.sh)
fi

cd "$DIR"
exec java \
    -Xms"$RAM" -Xmx"$RAM" \
    -XX:+UseG1GC \
    -XX:+ParallelRefProcEnabled \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+DisableExplicitGC \
    -XX:+AlwaysPreTouch \
    -jar "$JAR" nogui
