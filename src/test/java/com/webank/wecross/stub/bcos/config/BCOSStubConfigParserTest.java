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
        BCOSStubConfig.ChainRpcService chainRpcService = bcosStubConfig.getChannelService();
        assertTrue(Objects.nonNull(chainRpcService.getChain()));
        assertEquals(chainRpcService.getChain().getChainID(), "chain0");
        assertEquals(chainRpcService.getChain().getGroupID(), "group0");
        assertEquals(chainRpcService.getTimeout(), 111100);
        assertEquals(chainRpcService.getCaCert(), "./" + File.separator + "ca.crt");
        assertEquals(chainRpcService.getSslCert(), "./" + File.separator + "sdk.crt");
        assertEquals(chainRpcService.getSslKey(), "./" + File.separator + "sdk.key");

        assertEquals(chainRpcService.getConnectionsStr().size(), 1);

        assertEquals(chainRpcService.getThreadNum(), 8);
        assertEquals(chainRpcService.getQueueCapacity(), 5000);

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
