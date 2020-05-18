#!/bin/bash
set -e

echo "127.0.0.1:4 agency1 1,2,3" > ipconf

curl -LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.4.0/build_chain.sh && chmod u+x build_chain.sh
bash build_chain.sh -f ipconf
bash nodes/127.0.0.1/start_all.sh
./nodes/127.0.0.1/fisco-bcos -v

# Verify format
bash gradlew verifyGoogleJavaFormat
# gradle build check
bash gradlew build
bash gradlew test

# integration testing
mkdir -p src/integTest/resources/chains/bcos
cp -r nodes/127.0.0.1/sdk/* src/integTest/resources/chains/bcos
cp src/test/resources/stub.toml src/integTest/resources/chains/bcos/
cp -r src/test/resources/accounts src/integTest/resources/
cp -r src/test/resources/contract src/integTest/resources/
bash gradlew integTest
bash gradlew jacocoTestReport --info
