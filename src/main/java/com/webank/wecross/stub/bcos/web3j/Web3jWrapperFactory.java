package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;

public class Web3jWrapperFactory {
    public static Web3jWrapper build(BCOSStubConfig.ChannelService channelServiceConfig)
            throws Exception {
        return new Web3jWrapperImpl(channelServiceConfig);
    }
}
