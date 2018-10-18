#!/bin/sh

lein uberjar
mv -f target/fetch-cc-data-0.1.0-SNAPSHOT-standalone.jar distrib/fetch-cc-data-0.1.0.jar
cd distrib; zip -rT ../target/fetch-cc-data.zip *
cd ..
rm -f distrib/fetch-cc-data-0.1.0.jar
echo "Distribution created: target/fetch-cc-data.zip"
