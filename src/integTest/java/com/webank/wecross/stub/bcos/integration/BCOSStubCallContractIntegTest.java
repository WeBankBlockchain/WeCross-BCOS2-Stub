package com.webank.wecross.stub.bcos.integration;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSDriver;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImpl;

import java.io.IOException;
import java.util.Objects;

import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
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

    public TransactionContext<TransactionRequest> createTransactionRequestContext(
            String method, String[] args) {
        TransactionRequest transactionRequest =
                new TransactionRequest(method, args);
        TransactionContext<TransactionRequest> requestTransactionContext =
                new TransactionContext<>(
                        transactionRequest, account, resourceInfo, blockHeaderManager);
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
        account = bcosStubFactory.newAccount("IntegBCOSAccount", "classpath:/accounts/bcos");
        connection = bcosStubFactory.newConnection("./chains/bcos/");

        Web3jWrapper web3jWrapper = ((BCOSConnection) connection).getWeb3jWrapper();
        Web3jWrapperImpl web3jWrapperImpl = (Web3jWrapperImpl) web3jWrapper;

        BCOSAccount bcosAccount = (BCOSAccount) account;
        blockHeaderManager = new IntegTestBlockHeaderManagerImpl(web3jWrapper);

        helloWeCross =
                HelloWeCross
                        .deploy(
                                web3jWrapperImpl.getWeb3j(),
                                bcosAccount.getCredentials(),
                                new StaticGasProvider(SignTransaction.gasPrice, SignTransaction.gasLimit))
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
    public void getBlockNumberIntegIntegTest() {
        long blockNumber = driver.getBlockNumber(connection);
        assertTrue(blockNumber > 0);
    }

    @Test
    public void getBlockHeaderIntegTest() {
        long blockNumber = driver.getBlockNumber(connection);
        assertTrue(blockNumber > 0);
        byte[] blockHeader = driver.getBlockHeader(blockNumber, connection);
        assertTrue(blockHeader.length > 0);
        BlockHeader blockHeader1 = driver.decodeBlockHeader(blockHeader);
        assertTrue(Objects.nonNull(blockHeader1));
        assertTrue(Objects.nonNull(blockHeader1.getHash()));
        assertTrue(Objects.nonNull(blockHeader1.getReceiptRoot()));
        assertTrue(Objects.nonNull(blockHeader1.getTransactionRoot()));
        assertTrue(Objects.nonNull(blockHeader1.getPrevHash()));
        assertTrue(Objects.nonNull(blockHeader1.getStateRoot()));
        assertTrue(blockHeader1.getNumber() == blockNumber);
    }

    @Test
    public void getBlockHeaderFailedIntegTest() {
        long blockNumber = driver.getBlockNumber(connection);
        assertTrue(blockNumber > 0);
        byte[] blockHeader = driver.getBlockHeader(blockNumber + 1, connection);
        assertTrue(Objects.isNull(blockHeader));
    }

    @Test
    public void callIntegTest() throws TransactionException {
        String[] params = null;
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("get", params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse.getResult().length == 0);
    }

    @Test
    public void callNotExistMethodIntegTest() throws TransactionException {
        String[] params = new String[]{"aa", "bb", "cc", "dd"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("getNotExist", params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.HandleCallRequestFailed);
        assertTrue(Objects.isNull(transactionResponse.getResult()));
    }

    @Test
    public void sendTransactionIntegTest() throws TransactionException {
        String[] params = new String[]{"aa", "bb", "cc", "dd"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("set", params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse.getBlockNumber() > 0);
        assertTrue(transactionResponse.getResult().length == params.length);
        for (int i=0;i<transactionResponse.getResult().length;++i) {
            assertEquals(transactionResponse.getResult()[i], params[i]);
        }

        TransactionContext<TransactionRequest> requestTransactionContext0 =
                createTransactionRequestContext("get", null);
        TransactionResponse transactionResponse0 =
                driver.call(requestTransactionContext0, connection);

        assertTrue(transactionResponse0.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse0.getResult().length == params.length);

        TransactionContext<TransactionRequest> getRequestTransactionContext =
                createTransactionRequestContext("get", null);
        TransactionResponse getTransactionResponse =
                driver.call(getRequestTransactionContext, connection);

        assertTrue(getTransactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(getTransactionResponse.getResult().length == params.length);
        for (int i=0;i<getTransactionResponse.getResult().length;++i) {
            assertEquals(getTransactionResponse.getResult()[i], params[i]);
        }
    }

    @Test
    public void sendTransactionNotExistIntegTest() throws TransactionException {
        String[] params = new String[] {"aa", "bb", "cc", "dd"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("setNotExist", params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.SendTransactionNotSuccessStatus);
    }

    @Test
    public void emptyParamsSendTransactionIntegTest() throws TransactionException {
        String[] params = new String[0];
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("set", params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse.getResult().length == params.length);

        TransactionContext<TransactionRequest> getRequestTransactionContext =
                createTransactionRequestContext("get", null);
        TransactionResponse getTransactionResponse =
                driver.call(getRequestTransactionContext, connection);

        assertTrue(getTransactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(getTransactionResponse.getResult().length == params.length);
        for (int i=0;i<getTransactionResponse.getResult().length;++i) {
            assertEquals(getTransactionResponse.getResult()[i], params[i]);
        }
    }

    @Test
    public void getTransactionReceiptTest() throws IOException, BCOSStubException, TransactionException {
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("getAndClear", null);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(Objects.nonNull(transactionResponse.getHash()));

        TransactionProof transactionProof = ((BCOSDriver) driver).requestTransactionProof(transactionResponse.getHash(), connection);
        TransactionReceipt transactionReceipt = transactionProof.getReceiptAndProof().getTransactionReceipt();

        assertEquals(transactionReceipt.getTransactionHash(), transactionResponse.getHash());
        assertEquals(transactionReceipt.getBlockNumber().longValue(), transactionResponse.getBlockNumber());

    }

    @Test
    public void getVerifiedTransactionEmptyParamsTest() throws IOException, BCOSStubException, TransactionException {
        String[] params = new String[0];
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("set", params);

        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);

        TransactionProof transactionProof = ((BCOSDriver) driver).requestTransactionProof(transactionResponse.getHash(), connection);
        TransactionReceipt transactionReceipt = transactionProof.getReceiptAndProof().getTransactionReceipt();
        Transaction transaction = transactionProof.getTransAndProof().getTransaction();
        assertEquals(transaction.getHash(), transactionReceipt.getTransactionHash());
        assertEquals(transaction.getBlockNumber().longValue(), transactionReceipt.getBlockNumber().longValue());

        VerifiedTransaction verifiedTransaction = driver.getVerifiedTransaction(transactionResponse.getHash(), transactionResponse.getBlockNumber(), blockHeaderManager, connection);

        assertEquals(verifiedTransaction.getBlockNumber(), transactionResponse.getBlockNumber());
        assertEquals(verifiedTransaction.getTransactionHash(), transactionResponse.getHash());
        assertEquals(verifiedTransaction.getRealAddress(), helloWeCross.getContractAddress());

        TransactionRequest transactionRequest = verifiedTransaction.getTransactionRequest();
        assertEquals(transactionRequest.getArgs().length, params.length);

        TransactionResponse transactionResponse1 = verifiedTransaction.getTransactionResponse();
        assertEquals(transactionResponse1.getErrorCode().intValue(), BCOSStatusCode.Success);
        assertEquals(transactionResponse1.getHash(), transactionReceipt.getTransactionHash());
        assertEquals(transactionResponse1.getBlockNumber(), transactionReceipt.getBlockNumber().longValue());
        assertEquals(transactionResponse1.getResult().length, params.length);
        for (int i=0;i<transactionResponse1.getResult().length;++i) {
            assertEquals(transactionResponse1.getResult()[i], params[i]);
        }
    }

    @Test
    public void getVerifiedTransactionTest() throws IOException, BCOSStubException, TransactionException {
        String[] params = new String[] {"aa", "bb", "cc", "dd"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("set", params);

        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(Objects.nonNull(transactionResponse.getHash()));

        TransactionProof transactionProof = ((BCOSDriver) driver).requestTransactionProof(transactionResponse.getHash(), connection);
        TransactionReceipt transactionReceipt = transactionProof.getReceiptAndProof().getTransactionReceipt();
        Transaction transaction = transactionProof.getTransAndProof().getTransaction();
        assertEquals(transaction.getHash(), transactionReceipt.getTransactionHash());
        assertEquals(transaction.getBlockNumber().longValue(), transactionReceipt.getBlockNumber().longValue());

        VerifiedTransaction verifiedTransaction = driver.getVerifiedTransaction(transactionResponse.getHash(), transactionResponse.getBlockNumber(), blockHeaderManager, connection);

        assertEquals(verifiedTransaction.getBlockNumber(), transactionResponse.getBlockNumber());
        assertEquals(verifiedTransaction.getTransactionHash(), transactionResponse.getHash());
        assertEquals(verifiedTransaction.getRealAddress(), helloWeCross.getContractAddress());

        TransactionRequest transactionRequest = verifiedTransaction.getTransactionRequest();
        assertEquals(transactionRequest.getArgs().length, params.length);

        TransactionResponse transactionResponse1 = verifiedTransaction.getTransactionResponse();
        assertEquals(transactionResponse1.getErrorCode().intValue(), 0);
        assertEquals(transactionResponse1.getHash(), transactionReceipt.getTransactionHash());
        assertEquals(transactionResponse1.getBlockNumber(), transactionReceipt.getBlockNumber().longValue());
        assertEquals(transactionResponse1.getResult().length, params.length);
        for (int i=0;i<transactionResponse1.getResult().length;++i) {
            assertEquals(transactionResponse1.getResult()[i], params[i]);
        }
    }

    @Test
    public void getVerifiedTransactionNotExistTest() {
        String transactionHash = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        VerifiedTransaction verifiedTransaction = driver.getVerifiedTransaction(transactionHash, 1, blockHeaderManager, connection);
        assertTrue(Objects.isNull(verifiedTransaction));
    }
}
