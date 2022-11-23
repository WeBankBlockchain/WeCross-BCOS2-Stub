package com.webank.wecross.stub.bcos.client;

public class ClientDefaultConfig {
    /** default groupId and chainId */
    public static final String DEFAULT_GROUP_ID = "group0";

    public static final String DEFAULT_CHAIN_ID = "chain0";

    /** ChainRpcService default timeout: 60s */
    public static final int CHANNEL_SERVICE_DEFAULT_TIMEOUT = 60000;

    /** ChainRpcService default thread number */
    public static final int CHANNEL_SERVICE_DEFAULT_THREAD_NUMBER = 16;

    public static final int CHANNEL_SERVICE_DEFAULT_THREAD_QUEUE_CAPACITY = 10000;
}
