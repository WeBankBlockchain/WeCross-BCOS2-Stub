package com.webank.wecross.stub.bcos.common;

/** The definition of the resource type in BCOS */
public class BCOSConstant {
    /** BCOS contract resource */
    public static final String RESOURCE_TYPE_BCOS_CONTRACT = "BCOS_CONTRACT";
    /** */
    public static final String BCOS_ACCOUNT = "BCOS2.0";
    /** */
    public static final String BCOS_SM_ACCOUNT = "BCOS2.0_gm";

    public static final String BCOS_RESOURCEINFO_GROUP_ID = "BCOS_PROPERTY_GROUP_ID";
    public static final String BCOS_RESOURCEINFO_CHAIN_ID = "BCOS_PROPERTY_CHAIN_ID";

    /** BCOS Request type */
    public static final int BCOS_CALL = 1000;

    public static final int BCOS_SEND_TRANSACTION = 1001;
    public static final int BCOS_GET_BLOCK_NUMBER = 1002;
    public static final int BCOS_GET_BLOCK_HEADER = 1003;

    /** default groupId and default chainId */
    public static final int BCOS_DEFAULT_GROUP_ID = 1;

    public static final int BCOS_DEFAULT_CHAIN_ID = 1;

    /** ChannelService default timeout: 60s */
    public static final int CHANNELSERVICE_TIMEOUT_DEFAULT = 60000;
    /** JavaSDK init, default timeout: 60s */
    public static final int WE3J_START_TIMEOUT_DEFAULT = 60000;
}
