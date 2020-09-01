package com.webank.wecross.stub.bcos;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImplMock;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class BCOSConnectionFactoryTest {
    @Test
    public void buildTest() throws IOException {
        try {

            BCOSStubConfigParser bcosStubConfigParser =
                    new BCOSStubConfigParser("./", "stub-sample-ut.toml");
            BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

            Connection connection =
                    BCOSConnectionFactory.build(bcosStubConfig, new Web3jWrapperImplMock());

            Driver driver = new BCOSDriver();
            List<ResourceInfo> resources = driver.getResources(connection);
            assertTrue(resources.size() == 3);
            ResourceInfo resourceInfo = resources.get(0);
            assertEquals(resourceInfo.getName(), "HelloWeCross");
            assertEquals(resourceInfo.getStubType(), "BCOS-UT");
            assertEquals(
                    resourceInfo.getProperties().get(resourceInfo.getName()),
                    "0x8827cca7f0f38b861b62dae6d711efe92a1e3602");
            assertEquals(resourceInfo.getProperties().get(BCOSConstant.BCOS_GROUP_ID), 111);
            assertEquals(resourceInfo.getProperties().get(BCOSConstant.BCOS_CHAIN_ID), 123);

            ResourceInfo resourceInfo0 = resources.get(1);
            assertEquals(resourceInfo0.getName(), "Hello");
            assertEquals(resourceInfo0.getStubType(), "BCOS-UT");
            assertEquals(
                    resourceInfo0.getProperties().get(resourceInfo0.getName()),
                    "0x8827cca7f0f38b861b62dae6d711efe92a1e3603");
            assertEquals(resourceInfo0.getProperties().get(BCOSConstant.BCOS_GROUP_ID), 111);
            assertEquals(resourceInfo0.getProperties().get(BCOSConstant.BCOS_CHAIN_ID), 123);
        } catch (Exception e) {
            assertFalse(true);
        }
    }
}
