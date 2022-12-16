package com.webank.wecross.stub.bcos3.client;

import com.webank.wecross.stub.bcos3.config.BCOSStubConfig;
import org.fisco.bcos.sdk.v3.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientWrapperFactory {

    private static final Logger logger = LoggerFactory.getLogger(ClientWrapperFactory.class);

    public static AbstractClientWrapper createClientWrapperInstance(BCOSStubConfig bcosStubConfig)
            throws Exception {
        logger.info("BCOSStubConfig: {}", bcosStubConfig);
        Client client = ClientUtility.initClient(bcosStubConfig);
        return new ClientWrapperImpl(client);
    }
}
