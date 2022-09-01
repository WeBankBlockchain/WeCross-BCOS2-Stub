package com.webank.wecross.stub.bcos.client;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class ClientDefaultConfigTest {
    @Test
    public void clientDefaultConfigTest() {
        assertEquals(ClientDefaultConfig.DEFAULT_GROUP_ID, 1);
        assertEquals(ClientDefaultConfig.DEFAULT_CHAIN_ID, 1);
        assertEquals(ClientDefaultConfig.CHANNEL_SERVICE_DEFAULT_TIMEOUT, 60000);
    }
}
