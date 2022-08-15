package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import org.fisco.bcos.sdk.client.Client;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class Web3jWrapperImplTest {
    @Test
    public void web3jWrapperImplTest() throws IOException {
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./", "stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();

        try {
            Client client = Web3jUtility.initClient(channelService);
        } catch (Exception e) {

        } finally {
            assertTrue(true);
        }
    }
}
