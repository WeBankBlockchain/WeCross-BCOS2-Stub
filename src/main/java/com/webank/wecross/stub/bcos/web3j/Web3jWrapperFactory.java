package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;

public class Web3jWrapperFactory {
    public static Web3jWrapper build(BCOSStubConfig.ChannelService channelServiceConfig)
            throws Exception {
        if (channelServiceConfig.getEnableTest()) {
            return new Web3jWrapperImpl();
        }
        return new Web3jWrapperImpl(channelServiceConfig);
    }
}
