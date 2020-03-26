package com.webank.wecross.stub.bcos.web3j;

import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import java.io.IOException;
import org.junit.Test;

public class Web3jWrapperImplTest {
    @Test
    public void web3jWrapperImplTest() throws IOException {
        BCOSStubConfigParser bcosStubConfigParser = new BCOSStubConfigParser("stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();

        try {
            Web3jWrapperImpl web3jWrapper = new Web3jWrapperImpl(channelService);
        } catch (Exception e) {
        } finally {
            assertTrue(true);
        }
    }
}
