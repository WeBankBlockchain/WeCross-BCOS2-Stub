package com.webank.wecross.stub.bcos3.config;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
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

        BCOSStubConfig.Chain chain = bcosStubConfig.getChain();
        assertTrue(Objects.nonNull(chain));
        assertEquals(chain.getChainID(), "chain0");
        assertEquals(chain.getGroupID(), "group0");

        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();
        assertEquals(channelService.getCaCert(), "./" + File.separator + "ca.crt");
        assertEquals(channelService.getSslCert(), "./" + File.separator + "sdk.crt");
        assertEquals(channelService.getSslKey(), "./" + File.separator + "sdk.key");
        assertFalse(channelService.isDisableSsl());
        assertEquals(channelService.getMessageTimeout(), 111100);
        assertEquals(channelService.getConnectionsStr().size(), 1);
        assertEquals(channelService.getThreadPoolSize(), 8);

        assertEquals(bcosStubConfig.getResources().size(), 2);
        assertEquals(bcosStubConfig.getResources().get(0).getName(), "HelloWeCross");
        assertEquals(bcosStubConfig.getResources().get(0).getType(), "BCOS_CONTRACT");
        assertEquals(
                bcosStubConfig.getResources().get(0).getValue(),
                "0x8827cca7f0f38b861b62dae6d711efe92a1e3602");

        assertEquals(bcosStubConfig.getResources().get(1).getName(), "Hello");
        assertEquals(bcosStubConfig.getResources().get(1).getType(), "BCOS_CONTRACT");
        assertEquals(
                bcosStubConfig.getResources().get(0).getValue(),
                "0x8827cca7f0f38b861b62dae6d711efe92a1e3602");
    }
}
