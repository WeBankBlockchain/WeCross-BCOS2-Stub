import static junit.framework.TestCase.assertEquals;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import java.io.IOException;
import org.junit.Test;

public class BCOSStubConfigParserTest {
    @Test
    public void stubConfigParserTest() throws IOException {
        BCOSStubConfigParser bcosStubConfigParser = new BCOSStubConfigParser("stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        assertEquals(bcosStubConfig.getStub(), "bcos-ut");
        assertEquals(bcosStubConfig.getType(), "BCOS-UT");
        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();
        assertEquals(channelService.getChain().getChainID(), 123);
        assertEquals(channelService.getChain().getGroupID(), 111);
        assertEquals(channelService.getChain().isEnableGM(), true);
        assertEquals(channelService.getTimeout(), 111100);
        assertEquals(channelService.getCaCert(), "classpath:/stubs/bcos/ca.crt");
        assertEquals(channelService.getSslCert(), "classpath:/stubs/bcos/sdk.crt");
        assertEquals(channelService.getSslKey(), "classpath:/stubs/bcos/sdk.key");

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
