package com.webank.wecross.stub.bcos.common;

/** The definition of the resource type in BCOS */
public interface BCOSConstant {
    /** BCOS contract resource */
    String RESOURCE_TYPE_BCOS_CONTRACT = "BCOS_CONTRACT";
    /** */
    String BCOS_ACCOUNT = "BCOS2.0";
    /** */
    String BCOS_SM_ACCOUNT = "GM_BCOS2.0";

    String BCOS_GROUP_ID = "BCOS_PROPERTY_GROUP_ID";
    String BCOS_CHAIN_ID = "BCOS_PROPERTY_CHAIN_ID";
    String BCOS_STUB_TYPE = "BCOS_PROPERTY_STUB_TYPE";
    String BCOS_PROXY_NAME = "WeCrossProxy";
    String TRANSACTION_ID = "transaction_id";
    String TRANSACTION_SEQ = "transaction_seq";

    String CUSTOM_COMMAND_DEPLOY = "deploy";
    String CUSTOM_COMMAND_REGISTER = "register";
    String METHOD_DEPLOY = "deploy";
    String CNS_PRECOMPILED_ADDRESS = "0x0000000000000000000000000000000000001004";
    String DEFAULT_ADDRESS = "0x1111111111111111111111111111111111111111";
    String CNS_METHOD_INSERT = "insert";
    String CNS_METHOD_SELECTBYNAME = "selectByName";
    String CNS_METHOD_SELECTBYNAMEANDVERSION = "selectByNameAndVersion";
    String PROXY_METHOD_ADDPATH = "addPath";
    String PROXY_METHOD_GETPATHS = "getPaths";
}
