package com.webank.wecross.stub.bcos3.common;

import com.webank.wecross.stub.StubConstant;

/** The definition of the resource type in BCOS */
public interface BCOSConstant {
    String ADMIN_ACCOUNT = "admin";

    String SECP256K1 = "secp256k1";
    String SM2P256V1 = "sm2p256v1";

    String RESOURCE_TYPE_BCOS_CONTRACT = "BCOS_CONTRACT";

    String WASM = "WASM";
    String GM = "GM";

    String BCOS3_ECDSA_EVM_STUB_TYPE = "BCOS3_ECDSA_EVM";
    String BCOS3_ECDSA_WASM_STUB_TYPE = "BCOS3_ECDSA_WASM";
    String BCOS3_GM_EVM_STUB_TYPE = "BCOS3_GM_EVM";
    String BCOS3_GM_WASM_STUB_TYPE = "BCOS3_GM_WASM";

    String BCOS_GROUP_ID = "BCOS_PROPERTY_GROUP_ID";
    String BCOS_CHAIN_ID = "BCOS_PROPERTY_CHAIN_ID";
    String BCOS_STUB_TYPE = "BCOS_PROPERTY_STUB_TYPE";

    String BCOS_SEALER_LIST = "VERIFIER";
    int BCOS_NODE_ID_LENGTH = 128;

    String BCOS_PROXY_ABI = "WeCrossProxyABI";
    String BCOS_PROXY_NAME = StubConstant.PROXY_NAME;
    String BCOS_HUB_NAME = StubConstant.HUB_NAME;

    String CUSTOM_COMMAND_DEPLOY = "deploy";
    String CUSTOM_COMMAND_DEPLOY_WASM = "deployWasm";
    String CUSTOM_COMMAND_REGISTER = "register";
    String DEFAULT_ADDRESS = "0x1111111111111111111111111111111111111111";
    String PROXY_METHOD_DEPLOY = "deployContractWithRegisterBFS";
    String PROXY_METHOD_REGISTER = "linkBFS";
    String PROXY_METHOD_GETPATHS = "getPaths";
    String PROXY_METHOD_READLINK = "readlink";
}
