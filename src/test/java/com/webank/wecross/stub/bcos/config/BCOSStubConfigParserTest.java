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
        BCOSStubConfigParser bcosStubConfigParser = new BCOSStubConfigParser("./");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        assertEquals(bcosStubConfig.getType(), "BCOS-UT");
        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();
        assertTrue(Objects.nonNull(channelService.getChain()));
        assertEquals(channelService.getChain().getChainID(), 123);
        assertEquals(channelService.getChain().getGroupID(), 111);
        assertEquals(channelService.getChain().isEnableGM(), false);
        assertEquals(channelService.getTimeout(), 111100);
        assertEquals(channelService.getCaCert(), "./" + File.separator + "ca.crt");
        assertEquals(channelService.getSslCert(), "./" + File.separator + "sdk.crt");
        assertEquals(channelService.getSslKey(), "./" + File.separator + "sdk.key");

        assertEquals(channelService.getConnectionsStr().size(), 4);

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
