import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSConnectionFactory;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImpl;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSStubCallContractIntegTest {

    private static final Logger logger =
            LoggerFactory.getLogger(BCOSStubCallContractIntegTest.class);

    private HelloWeCross helloWeCross = null;

    private Driver driver = null;
    private Account account = null;
    private Connection connection = null;
    private ResourceInfo resourceInfo = null;
    private BlockHeaderManager blockHeaderManager = null;

    public HelloWeCross getHelloWeCross() {
        return helloWeCross;
    }

    public void setHelloWeCross(HelloWeCross helloWeCross) {
        this.helloWeCross = helloWeCross;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    public BlockHeaderManager getBlockHeaderManager() {
        return blockHeaderManager;
    }

    public void setBlockHeaderManager(BlockHeaderManager blockHeaderManager) {
        this.blockHeaderManager = blockHeaderManager;
    }

    public TransactionContext<TransactionRequest> creteTranscactionRequestContext(
            String method, List<String> args) {
        TransactionRequest transactionRequest =
                new TransactionRequest(method, args.toArray(new String[0]));
        TransactionContext<TransactionRequest> requestTransactionContext =
                new TransactionContext<TransactionRequest>(
                        transactionRequest, account, resourceInfo);
        requestTransactionContext.setAccount(account);
        requestTransactionContext.setBlockHeaderManager(blockHeaderManager);
        requestTransactionContext.setData(transactionRequest);
        requestTransactionContext.setResourceInfo(resourceInfo);
        return requestTransactionContext;
    }

    @Before
    public void initializer() throws Exception {

        BCOSStubFactory bcosStubFactory = new BCOSStubFactory();
        driver = bcosStubFactory.newDriver();
        account = BCOSAccountFactory.build("IntegBCOSAccount", "accounts/bcos");
        connection = BCOSConnectionFactory.build("stub-sample.toml");

        Web3jWrapper web3jWrapper = ((BCOSConnection) connection).getWeb3jWrapper();
        Web3jWrapperImpl web3jWrapperImpl = (Web3jWrapperImpl) web3jWrapper;
        BCOSAccount bcosAccount = (BCOSAccount) account;
        blockHeaderManager = new IntegTestBlockHeaderManagerImpl(web3jWrapper);

        helloWeCross =
                this.helloWeCross
                        .deploy(
                                web3jWrapperImpl.getWeb3j(),
                                bcosAccount.getCredentials(),
                                SignTransaction.gasPrice,
                                SignTransaction.gasPrice)
                        .send();

        logger.info(" HelloWeCross address: {}", helloWeCross.getContractAddress());

        resourceInfo = ((BCOSConnection) connection).getResourceInfoList().get(0);
        resourceInfo.getProperties().put(resourceInfo.getName(), helloWeCross.getContractAddress());

        logger.info(
                " ResourceInfo name: {}, type: {}, properties: {}",
                resourceInfo.getName(),
                resourceInfo.getStubType(),
                resourceInfo.getProperties());
    }

    @Test
    public void getBlockNumberIntegIntegTest() throws Exception {
        TransactionContext<TransactionRequest> requestTransactionContext =
                creteTranscactionRequestContext("getBlockNumber", Arrays.asList());
        long blockNumber = driver.getBlockNumber(connection);
        assertTrue(blockNumber > 0);
    }

    @Test
    public void getBlockHeaderIntegTest() throws Exception {
        TransactionContext<TransactionRequest> requestTransactionContext =
                creteTranscactionRequestContext("getBlockHeader", Arrays.asList());
        byte[] blockHeader = driver.getBlockHeader(1, connection);
        assertTrue(blockHeader.length > 0);
        BlockHeader blockHeader1 = driver.decodeBlockHeader(blockHeader);
        logger.info(
                " BlockHeader => blockNumber: {}, hash: {}, prehash: {}, state: {}, receipt: {}, transaction :{}",
                blockHeader1.getNumber(),
                blockHeader1.getHash(),
                blockHeader1.getPrevHash(),
                blockHeader1.getStateRoot(),
                blockHeader1.getReceiptRoot(),
                blockHeader1.getTransactionRoot());
    }

    @Test
    public void callIntegTest() throws Exception {
        List<String> params = Arrays.asList("aa", "bb", "cc", "dd");
        TransactionContext<TransactionRequest> requestTransactionContext =
                creteTranscactionRequestContext("get", params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == 0);
        assertTrue(transactionResponse.getResult().length == params.size());
    }

    @Test
    public void sendTransactionIntegTest() throws Exception {
        List<String> params = Arrays.asList("aa", "bb", "cc", "dd");
        TransactionContext<TransactionRequest> requestTransactionContext =
                creteTranscactionRequestContext("set", params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == 0);
        assertTrue(transactionResponse.getResult().length == params.size());
    }
}
