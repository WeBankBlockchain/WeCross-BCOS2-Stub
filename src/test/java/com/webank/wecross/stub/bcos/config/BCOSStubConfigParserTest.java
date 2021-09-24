package com.webank.wecross.stub.bcos.config;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.junit.Test;

public class BCOSStubConfigParserTest {
    @Test
    public void stubConfigParserTest() throws IOException {
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./", "stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        assertEquals(bcosStubConfig.getType(), "BCOS-UT");
        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();
        assertTrue(Objects.nonNull(channelService.getChain()));
        assertEquals(channelService.getChain().getChainID(), 123);
        assertEquals(channelService.getChain().getGroupID(), 111);
        assertEquals(channelService.getTimeout(), 111100);
        assertEquals(channelService.getCaCert(), "./" + File.separator + "ca.crt");
        assertEquals(channelService.getSslCert(), "./" + File.separator + "sdk.crt");
        assertEquals(channelService.getSslKey(), "./" + File.separator + "sdk.key");

        assertEquals(channelService.getConnectionsStr().size(), 1);

        assertEquals(channelService.getThreadNum(), 8);
        assertEquals(channelService.getQueueCapacity(), 5000);
    }
}
