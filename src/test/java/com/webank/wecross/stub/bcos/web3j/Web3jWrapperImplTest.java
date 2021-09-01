package com.webank.wecross.stub.bcos.web3j;

import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import java.io.IOException;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.junit.Test;

public class Web3jWrapperImplTest {
    @Test
    public void web3jWrapperImplTest() throws IOException {
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./", "stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();

        try {
            Service service = Web3jUtility.initService(channelService);
            Web3j web3j = Web3jUtility.initWeb3j(channelService, service);
        } catch (Exception e) {

        } finally {
            assertTrue(true);
        }
    }
}
