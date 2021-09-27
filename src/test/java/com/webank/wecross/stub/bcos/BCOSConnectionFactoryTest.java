package com.webank.wecross.stub.bcos;

import static junit.framework.TestCase.assertFalse;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImplMock;
import java.io.IOException;
import org.junit.Test;

public class BCOSConnectionFactoryTest {
    @Test
    public void buildTest() throws IOException {
        try {

            BCOSStubConfigParser bcosStubConfigParser =
                    new BCOSStubConfigParser("./", "stub-sample-ut.toml");
            BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

            BCOSConnectionFactory.build(bcosStubConfig, new Web3jWrapperImplMock());
        } catch (Exception e) {
            assertFalse(true);
        }
    }
}
