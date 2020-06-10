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

    String PROXY_CONTRACT = "WeCrossProxy";
    String CNS_PRECOMPILED_ADDRESS = "0x0000000000000000000000000000000000001004";
    String CNS_INSERT_METHOD = "insert";
    String DEPLOY_METHOD = "deploy";
}
