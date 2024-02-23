#!/bin/bash
set -e

# gradle build check
bash gradlew build
get_sed_cmd()
{
  local sed_cmd="sed -i"
  if [ "$(uname)" == "Darwin" ];then
        sed_cmd="sed -i .bkp"
  fi
  echo "$sed_cmd"
}
sed_cmd=$(get_sed_cmd)
# Non SM node test
echo " Not SM NODE ============>>>> "
echo " Not SM NODE ============>>>> "
echo " Not SM NODE ============>>>> "

# download build_chain.sh to build fisco-bcos block chain
curl -LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.10.1/build_chain.sh && chmod u+x build_chain.sh
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
bash gradlew jacocoTestReport

# clean
bash nodes/127.0.0.1/stop_all.sh
bash nodes/127.0.0.1/stop_all.sh
bash nodes/127.0.0.1/stop_all.sh
rm -rf nodes

# SM node test
echo " SM NODE ============>>>> "
echo " SM NODE ============>>>> "
echo " SM NODE ============>>>> "

# download build_chain.sh to build fisco-bcos block chain
curl -LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.10.1/build_chain.sh && chmod u+x build_chain.sh
echo "127.0.0.1:4 agency1 1,2,3" > ipconf
bash build_chain.sh -f ipconf -g
./nodes/127.0.0.1/fisco-bcos -v
cat nodes/127.0.0.1/node0/config.ini | egrep sm_crypto

bash nodes/127.0.0.1/start_all.sh
# integration testing
mkdir -p src/integTest/resources/chains/bcos
cp -r nodes/127.0.0.1/sdk/* src/integTest/resources/chains/bcos
cp src/test/resources/stub.toml src/integTest/resources/chains/bcos/
${sed_cmd} 's/BCOS2/GM_BCOS2/g' src/integTest/resources/chains/bcos/stub.toml

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

# SM node SM SSL test
echo " SM NODE, SM SSL============>>>> "
echo " SM NODE, SM SSL============>>>> "
echo " SM NODE, SM SSL============>>>> "

# download build_chain.sh to build fisco-bcos block chain
curl -LO https://github.com/FISCO-BCOS/FISCO-BCOS/releases/download/v2.10.1/build_chain.sh && chmod u+x build_chain.sh
echo "127.0.0.1:4 agency1 1,2,3" > ipconf
bash build_chain.sh -f ipconf -g -G
./nodes/127.0.0.1/fisco-bcos -v
cat nodes/127.0.0.1/node0/config.ini | egrep sm_crypto

bash nodes/127.0.0.1/start_all.sh
# integration testing
mkdir -p src/integTest/resources/chains/bcos
cp -r nodes/127.0.0.1/sdk/* src/integTest/resources/chains/bcos
cp src/test/resources/stub.toml src/integTest/resources/chains/bcos/
${sed_cmd} -e 's/BCOS2/GM_BCOS2/g' src/integTest/resources/chains/bcos/stub.toml
${sed_cmd} -e 's/gmConnectEnable = false/gmConnectEnable = true/g' src/integTest/resources/chains/bcos/stub.toml

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
