package com.webank.wecross.stub.bcos.common;

/** The definition of the resource type in BCOS */
public interface BCOSConstant {
    /** BCOS contract resource */
    String RESOURCE_TYPE_BCOS_CONTRACT = "BCOS_CONTRACT";
    /** */
    String BCOS_ACCOUNT = "BCOS2.0";
    /** */
    String BCOS_SM_ACCOUNT = "BCOS2.0_gm";

    String BCOS_RESOURCEINFO_GROUP_ID = "BCOS_PROPERTY_GROUP_ID";
    String BCOS_RESOURCEINFO_CHAIN_ID = "BCOS_PROPERTY_CHAIN_ID";

    /** default groupId and default chainId */
    int BCOS_DEFAULT_GROUP_ID = 1;

    int BCOS_DEFAULT_CHAIN_ID = 1;

    /** ChannelService default timeout: 60s */
    int CHANNELSERVICE_TIMEOUT_DEFAULT = 60000;
    /** JavaSDK init, default timeout: 60s */
    int WE3J_START_TIMEOUT_DEFAULT = 60000;
}
