package com.webank.wecross.stub.bcos.common;

import com.webank.wecross.stub.StubConstant;

/** The definition of the resource type in BCOS */
public interface BCOSConstant {
    String ADMIN_ACCOUNT = "admin";

    /** BCOS contract resource */
    String RESOURCE_TYPE_BCOS_CONTRACT = "BCOS_CONTRACT";
    /** */
    String BCOS_ACCOUNT = "BCOS2.0";
    /** */
    String BCOS_SM_ACCOUNT = "GM_BCOS2.0";

    String BCOS_GROUP_ID = "BCOS_PROPERTY_GROUP_ID";
    String BCOS_CHAIN_ID = "BCOS_PROPERTY_CHAIN_ID";
    String BCOS_STUB_TYPE = "BCOS_PROPERTY_STUB_TYPE";
    String BCOS_NODE_VERSION = "BCOS_PROPERTY_NODE_VERSION";

    String BCOS_SEALER_LIST = "VERIFIER";
    int BCOS_NODE_ID_LENGTH = 128;

    String BCOS_PROXY_ABI = "WeCrossProxyABI";
    String BCOS_PROXY_NAME = StubConstant.PROXY_NAME;
    String BCOS_HUB_NAME = StubConstant.HUB_NAME;
    String BCOS_CNS_NAME = "WeCrossCNS";

    String CUSTOM_COMMAND_DEPLOY = "deploy";
    String CUSTOM_COMMAND_REGISTER = "register";
    String CNS_PRECOMPILED_ADDRESS = "0x0000000000000000000000000000000000001004";
    String CNS_ABI =
            "[{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"}],\"name\":\"selectByName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"}],\"name\":\"selectByNameAndVersion\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"addr\",\"type\":\"string\"},{\"name\":\"abi\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"}],\"name\":\"getContractAddress\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";
    String DEFAULT_ADDRESS = "0x1111111111111111111111111111111111111111";
    String CNS_METHOD_SELECTBYNAME = "selectByName";
    String PROXY_METHOD_DEPLOY = "deployContractWithRegisterCNS";
    String PPROXY_METHOD_REGISTER = "registerCNS";
    String PROXY_METHOD_GETPATHS = "getPaths";
}
