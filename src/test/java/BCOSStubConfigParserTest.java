import static junit.framework.TestCase.assertEquals;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import java.io.IOException;
import org.junit.Test;

public class BCOSStubConfigParserTest {
    @Test
    public void transactionSignTest() throws IOException {
        BCOSStubConfigParser bcosStubConfigParser = new BCOSStubConfigParser("stub-sample.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        assertEquals(bcosStubConfig.getStub(), "bcos");
        assertEquals(bcosStubConfig.getType(), "BCOS");
        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();
        assertEquals(channelService.getChainID(), 1);
        assertEquals(channelService.getGroupID(), 1);
        assertEquals(channelService.getTimeout(), 300000);
        assertEquals(channelService.getCaCert(), "classpath:/stubs/bcos/ca.crt");
        assertEquals(channelService.getSslCert(), "classpath:/stubs/bcos/sdk.crt");
        assertEquals(channelService.getSslKey(), "classpath:/stubs/bcos/sdk.key");

        assertEquals(channelService.getConnectionsStr().size(), 3);

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
