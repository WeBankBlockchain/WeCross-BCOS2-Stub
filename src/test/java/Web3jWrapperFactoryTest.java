import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperFactory;
import java.io.IOException;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3jWrapperFactoryTest {

    private static Logger logger = LoggerFactory.getLogger(Web3jWrapperFactory.class);

    public static void main(String[] args) throws IOException {
        BCOSStubConfigParser bcosStubConfigParser = new BCOSStubConfigParser("stub-sample.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();

        try {
            Web3jWrapper web3jWrapper = Web3jWrapperFactory.build(channelService);
            BlockNumber blockNumber = web3jWrapper.getBlockNumber();
            BcosBlock bcosBlock =
                    web3jWrapper.getBlockByNumber(blockNumber.getBlockNumber().longValue());
            System.out.println(blockNumber.getBlockNumber());
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            SignTransaction signTransaction =
                    new SignTransaction(GenCredential.create(), blockNumber.getBlockNumber());
            String signTX = signTransaction.sign("0x00", "0x000");
            TransactionReceipt receipt = web3jWrapper.sendTransaction(signTX);
            System.out.println(receipt);

            System.out.println(
                    ObjectMapperFactory.getObjectMapper().writeValueAsString(bcosBlock.getBlock()));
        } catch (Exception e) {
            logger.error(" e: {}", e);
        }
    }
}
