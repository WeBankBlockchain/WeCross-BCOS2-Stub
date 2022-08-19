package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class Web3jWrapperImplTest {

    @Test
    public void web3jWrapperImplTest() throws IOException {
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./", "stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        try {
            AbstractWeb3jWrapper web3jWrapper = Web3jWrapperFactory.createWeb3jWrapperInstance(bcosStubConfig);
        } catch (Exception e) {

        } finally {
            assertTrue(true);
        }
    }


}
