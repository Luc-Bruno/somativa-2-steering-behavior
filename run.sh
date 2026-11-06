#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes dist
find src/main/java -name '*.java' > build/sources.txt
javac -encoding UTF-8 --release 17 -d build/classes @build/sources.txt
jar --create --file dist/ParqueAutonomo.jar --main-class br.edu.steering.App -C build/classes .
java -jar dist/ParqueAutonomo.jar
