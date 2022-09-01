package com.webank.wecross.stub.bcos.common;

import com.webank.wecross.stub.bcos.client.ClientDefaultConfig;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class BCOSConstantTest {

    @Test
    public void constantValueTest() throws IOException {
        assertEquals(BCOSConstant.BCOS_CHAIN_ID, "BCOS_PROPERTY_CHAIN_ID");
        assertEquals(BCOSConstant.BCOS_GROUP_ID, "BCOS_PROPERTY_GROUP_ID");
        assertEquals(BCOSConstant.RESOURCE_TYPE_BCOS_CONTRACT, "BCOS_CONTRACT");
        assertEquals(BCOSConstant.BCOS_ACCOUNT, "BCOS2.0");
        assertEquals(BCOSConstant.BCOS_SM_ACCOUNT, "GM_BCOS2.0");

        assertEquals(ClientDefaultConfig.DEFAULT_GROUP_ID, 1);
        assertEquals(ClientDefaultConfig.DEFAULT_CHAIN_ID, 1);
        assertEquals(ClientDefaultConfig.CHANNEL_SERVICE_DEFAULT_TIMEOUT, 60000);
    }

    @Test
    public void bcosRequestTypeTest() throws IOException {

        assertEquals(BCOSRequestType.CALL, 1000);
        assertEquals(BCOSRequestType.SEND_TRANSACTION, 1001);
        assertEquals(BCOSRequestType.GET_BLOCK_NUMBER, 1002);
        assertEquals(BCOSRequestType.GET_BLOCK_BY_NUMBER, 1003);
        assertEquals(BCOSRequestType.GET_TRANSACTION_PROOF, 1004);
    }
}
