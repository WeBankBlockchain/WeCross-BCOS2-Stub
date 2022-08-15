package com.webank.wecross.stub.bcos.web3j;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class Web3jDefaultConfigTest {
    @Test
    public void web3jDefaultConfigTest() {
        assertEquals(Web3jDefaultConfig.DEFAULT_GROUP_ID, 1);
        assertEquals(Web3jDefaultConfig.DEFAULT_CHAIN_ID, 1);
        assertEquals(Web3jDefaultConfig.CHANNEL_SERVICE_DEFAULT_TIMEOUT, 60000);
    }
}
