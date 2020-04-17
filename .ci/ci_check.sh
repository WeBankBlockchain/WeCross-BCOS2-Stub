#!/bin/bash
set -e

curl -s https://raw.githubusercontent.com/FISCO-BCOS/FISCO-BCOS/release-2.3.0-bsn/tools/ci/download_bin.sh | bash -s -- -m -b release-2.3.0-bsn

# download build_chain.sh to build fisco-bcos block chain
curl -LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.3.0/build_chain.sh && chmod u+x build_chain.sh
echo "127.0.0.1:4 agency1 1,2,3" > ipconf

ls -lt

if [ -f "./bin/fisco-bcos" ];then
  bash build_chain.sh -f ipconf -e "./bin/fisco-bcos"
else
  bash build_chain.sh -f ipconf
fi

bash nodes/127.0.0.1/start_all.sh
./nodes/127.0.0.1/fisco-bcos -v

# Verify format
bash gradlew verifyGoogleJavaFormat

# gradle build check
bash gradlew build
bash gradlew test
bash gradlew jacocoTestReport

# integration testing
mkdir -p src/integTest/resources/chains/bcos
cp nodes/127.0.0.1/sdk/* src/integTest/resources/chains/bcos
cp src/test/resources/stub.toml src/integTest/resources/chains/bcos/
cp -r src/test/resources/accounts src/integTest/resources/
cp -r src/test/resources/contract src/integTest/resources/
bash gradlew integTest
./gradlew jacocoTestReport
