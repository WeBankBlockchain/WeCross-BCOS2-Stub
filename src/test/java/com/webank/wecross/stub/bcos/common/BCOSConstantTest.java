package com.webank.wecross.stub.bcos.common;

import static junit.framework.TestCase.assertEquals;

import java.io.IOException;
import org.junit.Test;

public class BCOSConstantTest {

    @Test
    public void constantValueTest() throws IOException {
        assertEquals(BCOSConstant.BCOS_RESOURCEINFO_CHAIN_ID, "BCOS_PROPERTY_CHAIN_ID");
        assertEquals(BCOSConstant.BCOS_RESOURCEINFO_GROUP_ID, "BCOS_PROPERTY_GROUP_ID");
        assertEquals(BCOSConstant.RESOURCE_TYPE_BCOS_CONTRACT, "BCOS_CONTRACT");
        assertEquals(BCOSConstant.BCOS_ACCOUNT, "BCOS2.0");
        assertEquals(BCOSConstant.BCOS_SM_ACCOUNT, "BCOS2.0_gm");
        assertEquals(BCOSConstant.BCOS_CALL, 1000);
        assertEquals(BCOSConstant.BCOS_SEND_TRANSACTION, 1001);
        assertEquals(BCOSConstant.BCOS_GET_BLOCK_NUMBER, 1002);

        assertEquals(BCOSConstant.BCOS_DEFAULT_GROUP_ID, 1);
        assertEquals(BCOSConstant.BCOS_DEFAULT_CHAIN_ID, 1);
        assertEquals(BCOSConstant.CHANNELSERVICE_TIMEOUT_DEFAULT, 60000);
        assertEquals(BCOSConstant.WE3J_START_TIMEOUT_DEFAULT, 60000);
    }
}
