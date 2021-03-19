#!/bin/bash

# clean dist directory
rm dist/chebi-discovery*.js

# node lib
sbt fullOptJS
# browser lib
sbt fullOptJS::webpack

# browser lib js
cp ./target/scala-2.13/scalajs-bundler/main/chebi-discovery-opt-bundle.js ./dist/chebi-discovery-web.js

sed -i "s#$(pwd)#com/github/metabohub#g" dist/*

# generate md5sum to check js libraries
cat $(find . -name *.scala | sort -V) | md5sum > dist/checksum

