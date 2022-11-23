package com.webank.wecross.stub.bcos.common;

import com.webank.wecross.stub.StubConstant;

/** The definition of the resource type in BCOS */
public interface BCOSConstant {
    String ADMIN_ACCOUNT = "admin";

    /** BCOS contract resource */
    String RESOURCE_TYPE_BCOS_CONTRACT = "BCOS_CONTRACT";
    /** */
    String BCOS_ACCOUNT = "BCOS3.0";
    /** */
    String BCOS_SM_ACCOUNT = "GM_BCOS3.0";

    String BCOS_GROUP_ID = "BCOS_PROPERTY_GROUP_ID";
    String BCOS_CHAIN_ID = "BCOS_PROPERTY_CHAIN_ID";
    String BCOS_STUB_TYPE = "BCOS_PROPERTY_STUB_TYPE";
    String BCOS_NODE_VERSION = "BCOS_PROPERTY_NODE_VERSION";

    String BCOS_SEALER_LIST = "VERIFIER";
    int BCOS_NODE_ID_LENGTH = 128;

    String BCOS_PROXY_ABI = "WeCrossProxyABI";
    String BCOS_PROXY_NAME = StubConstant.PROXY_NAME;
    String BCOS_HUB_NAME = StubConstant.HUB_NAME;

    String CUSTOM_COMMAND_DEPLOY = "deploy";
    String CUSTOM_COMMAND_REGISTER = "register";
    String BFS_PRECOMPILED_ADDRESS = "0x000000000000000000000000000000000000100e";
    String DEFAULT_ADDRESS = "0x1111111111111111111111111111111111111111";
    String BFS_METHOD_LIST = "list";
    String PROXY_METHOD_DEPLOY = "deployContractWithRegisterBFS";
    String PPROXY_METHOD_REGISTER = "linkBFS";
    String PROXY_METHOD_GETPATHS = "getPaths";
    String PROXY_METHOD_READLINK = "readlink";
}
