package com.webank.wecross.stub.bcos.client;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.TestCase.assertNotNull;

public class ClientWrapperImplTest {

    @Test
    public void clientWrapperImplTest() throws Exception {
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./", "stub_fy.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        AbstractClientWrapper clientWrapper = ClientWrapperFactory.createClientWrapperInstance(bcosStubConfig);
        BigInteger blockNumber = clientWrapper.getBlockNumber();
        assertNotNull(blockNumber);
    }


}
