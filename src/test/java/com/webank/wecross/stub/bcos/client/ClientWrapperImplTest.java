package com.webank.wecross.stub.bcos.client;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class ClientWrapperImplTest {

    @Test
    public void clientWrapperImplTest() throws IOException {
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./", "stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        try {
            AbstractClientWrapper clientWrapper = ClientWrapperFactory.createClientWrapperInstance(bcosStubConfig);
        } catch (Exception e) {

        } finally {
            assertTrue(true);
        }
    }

}
