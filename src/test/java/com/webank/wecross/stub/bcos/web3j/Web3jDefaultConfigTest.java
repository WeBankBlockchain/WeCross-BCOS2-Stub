package com.webank.wecross.stub.bcos.web3j;

import static junit.framework.TestCase.assertEquals;

import java.io.IOException;
import org.junit.Test;

public class Web3jDefaultConfigTest {
    @Test
    public void web3jDefaultConfigTest() throws IOException {
        assertEquals(Web3jDefaultConfig.DEFAULT_GROUP_ID, 1);
        assertEquals(Web3jDefaultConfig.DEFAULT_CHAIN_ID, 1);
        assertEquals(Web3jDefaultConfig.CHANNEL_SERVICE_DEFAULT_TIMEOUT, 60000);
    }
}
