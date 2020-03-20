import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperFactory;
import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3jWrapperFactoryTest {

    private static Logger logger = LoggerFactory.getLogger(Web3jWrapperFactory.class);

    public static void main0(String[] args) throws IOException {
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("ut/stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();

        try {
            Web3jWrapper web3jWrapper = Web3jWrapperFactory.build(channelService);
            BigInteger blockNumber = web3jWrapper.getBlockNumber();
            BcosBlock.Block bcosBlock = web3jWrapper.getBlockByNumber(blockNumber.longValue());
            System.out.println(blockNumber);
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            SignTransaction signTransaction = new SignTransaction(GenCredential.create());
            String signTX =
                    signTransaction.sign(
                            "0x8827cca7f0f38b861b62dae6d711efe92a1e3602", "0x000", blockNumber);
            TransactionReceipt receipt = web3jWrapper.sendTransaction(signTX);
            System.out.println(receipt);

            System.out.println(ObjectMapperFactory.getObjectMapper().writeValueAsString(receipt));
        } catch (Exception e) {
            logger.error(" e: {}", e);
        }
    }

    public static void main(String[] args) throws IOException {
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("ut/stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();

        try {
            Web3jWrapper web3jWrapper = new Web3jWrapperMock();
            BCOSConnection bcosConnection = new BCOSConnection(web3jWrapper);
            Request request = new Request();
            request.setType(BCOSConstant.BCOS_GET_BLOCK_NUMBER);
            Response response = bcosConnection.send(request);
            BigInteger blockNumber = new BigInteger(response.getData());
            System.out.println(" blockNumber :" + blockNumber.longValue());
        } catch (Exception e) {
            logger.error(" e: {}", e);
        }
    }
}
