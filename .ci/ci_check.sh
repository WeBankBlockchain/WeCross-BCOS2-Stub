#!/bin/bash
set -e

# gradle build check
bash gradlew build

# Non SM node test
echo " Not SM ============>>>> "
echo " Not SM ============>>>> "
echo " Not SM ============>>>> "

# download build_chain.sh to build fisco-bcos block chain
curl -LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.6.0/build_chain.sh && chmod u+x build_chain.sh
echo "127.0.0.1:4 agency1 1,2,3" > ipconf
bash build_chain.sh -f ipconf
./nodes/127.0.0.1/fisco-bcos -v
cat nodes/127.0.0.1/node0/config.ini | egrep sm_crypto

bash nodes/127.0.0.1/start_all.sh
# integration testing
mkdir -p src/integTest/resources/chains/bcos
cp -r nodes/127.0.0.1/sdk/* src/integTest/resources/chains/bcos
cp src/test/resources/stub.toml src/integTest/resources/chains/bcos/
cp -r src/test/resources/accounts src/integTest/resources/
cp -r src/test/resources/contract src/integTest/resources/
mkdir -p src/integTest/resources/solidity
cp src/main/resources/* src/integTest/resources/solidity
cp src/test/resources/contract/* src/integTest/resources/solidity

echo -e "\n[sealers]" >> src/integTest/resources/chains/bcos/stub.toml
echo "    pubKey = [" >> src/integTest/resources/chains/bcos/stub.toml
cat nodes/127.0.0.1/node0/conf/group.1.genesis | grep "node\."| sed  -e 's/=/ /g; s/node.//g' | awk '{print $2}' | while read line ; do
    echo "    \"${line}\"," >> src/integTest/resources/chains/bcos/stub.toml
done
echo "    ]" >> src/integTest/resources/chains/bcos/stub.toml

bash gradlew integTest
bash gradlew jacocoTestReport

# clean
bash nodes/127.0.0.1/stop_all.sh
bash nodes/127.0.0.1/stop_all.sh
bash nodes/127.0.0.1/stop_all.sh
rm -rf nodes

# node without config [sealer] test
echo " NoSealer ============>>>> "
echo " NoSealer ============>>>> "
echo " NoSealer ============>>>> "

# download build_chain.sh to build fisco-bcos block chain
curl -LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.6.0/build_chain.sh && chmod u+x build_chain.sh
echo "127.0.0.1:4 agency1 1,2,3" > ipconf
bash build_chain.sh -f ipconf
./nodes/127.0.0.1/fisco-bcos -v
cat nodes/127.0.0.1/node0/config.ini | egrep sm_crypto

bash nodes/127.0.0.1/start_all.sh
# integration testing
mkdir -p src/integTest/resources/chains/bcos
cp -r nodes/127.0.0.1/sdk/* src/integTest/resources/chains/bcos
cp src/test/resources/stub.toml src/integTest/resources/chains/bcos/
cp -r src/test/resources/accounts src/integTest/resources/
cp -r src/test/resources/contract src/integTest/resources/
mkdir -p src/integTest/resources/solidity
cp src/main/resources/* src/integTest/resources/solidity
cp src/test/resources/contract/* src/integTest/resources/solidity

bash gradlew integTest

# clean
bash nodes/127.0.0.1/stop_all.sh
bash nodes/127.0.0.1/stop_all.sh
bash nodes/127.0.0.1/stop_all.sh
rm -rf nodes

# SM node test
echo " SM ============>>>> "
echo " SM ============>>>> "
echo " SM ============>>>> "

# download build_chain.sh to build fisco-bcos block chain
curl -LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.6.0/build_chain.sh && chmod u+x build_chain.sh
echo "127.0.0.1:4 agency1 1,2,3" > ipconf
bash build_chain.sh -f ipconf -g
./nodes/127.0.0.1/fisco-bcos -v
cat nodes/127.0.0.1/node0/config.ini | egrep sm_crypto

bash nodes/127.0.0.1/start_all.sh
# integration testing
mkdir -p src/integTest/resources/chains/bcos
cp -r nodes/127.0.0.1/sdk/* src/integTest/resources/chains/bcos
cp src/test/resources/stub.toml src/integTest/resources/chains/bcos/
sed -i.bak 's/BCOS2/GM_BCOS2/g' src/integTest/resources/chains/bcos/stub.toml

echo -e "\n[sealers]" >> src/integTest/resources/chains/bcos/stub.toml
echo "    pubKey = [" >> src/integTest/resources/chains/bcos/stub.toml
cat nodes/127.0.0.1/node0/conf/group.1.genesis | grep "node\."| sed  -e 's/=/ /g; s/node.//g' | awk '{print $2}' | while read line ; do
    echo "    \"${line}\"," >> src/integTest/resources/chains/bcos/stub.toml
done
echo "    ]" >> src/integTest/resources/chains/bcos/stub.toml

cat src/integTest/resources/chains/bcos/stub.toml
mkdir -p src/integTest/resources/accounts
cp -r src/test/resources/accounts/bcos src/integTest/resources/accounts
cp -r src/test/resources/accounts/gm_bcos src/integTest/resources/accounts/fisco
mkdir -p src/integTest/resources/solidity
cp src/main/resources/* src/integTest/resources/solidity
cp src/test/resources/contract/* src/integTest/resources/solidity
bash gradlew integTest

# clean
bash nodes/127.0.0.1/stop_all.sh
bash nodes/127.0.0.1/stop_all.sh
bash nodes/127.0.0.1/stop_all.sh
rm -rf nodes

bash <(curl -s https://codecov.io/bash)
