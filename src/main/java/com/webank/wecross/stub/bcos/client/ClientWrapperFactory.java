package com.webank.wecross.stub.bcos.client;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import org.fisco.bcos.sdk.v3.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientWrapperFactory {

    private static final Logger logger = LoggerFactory.getLogger(ClientWrapperFactory.class);

    public static AbstractClientWrapper createClientWrapperInstance(BCOSStubConfig bcosStubConfig)
            throws Exception {

        logger.info("BCOSStubConfig: {}", bcosStubConfig);

        Client client = ClientUtility.initClient(bcosStubConfig);

        AbstractClientWrapper clientWrapper = createClientWrapperInstance(client);
        clientWrapper.setVersion("3.0.0");

        return clientWrapper;
    }

    private static AbstractClientWrapper createClientWrapperInstance(Client client) {
        // default version
        return new ClientWrapperImpl(client);
    }
}
