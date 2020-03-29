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
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSConnectionFactory;
import com.webank.wecross.stub.bcos.BCOSDriver;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    public TransactionContext<TransactionRequest> createTxRequestContext(
            String method, List<String> args) {
        TransactionRequest transactionRequest =
                new TransactionRequest(method, args.toArray(new String[0]));
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
        connection = bcosStubFactory.newConnection("stub-sample.toml");

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
    public void callIntegTest() {
        List<String> params = Arrays.asList("aa", "bb", "cc", "dd");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTxRequestContext("get", params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == 0);
        assertTrue(transactionResponse.getResult().length == params.size());
        for (int i=0;i<transactionResponse.getResult().length;++i) {
            assertEquals(transactionResponse.getResult()[i], params.get(i));
        }
    }

    @Test
    public void emptyParamsCallIntegTest() {
        List<String> params = Arrays.asList();
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTxRequestContext("get", params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == 0);
        assertTrue(transactionResponse.getResult().length == params.size());
    }

    @Test
    public void sendTransactionIntegTest() {
        List<String> params = Arrays.asList("aa", "bb", "cc", "dd");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTxRequestContext("set", params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == 0);
        assertTrue(transactionResponse.getResult().length == params.size());
        for (int i=0;i<transactionResponse.getResult().length;++i) {
            assertEquals(transactionResponse.getResult()[i], params.get(i));
        }
    }

    @Test
    public void emptyParamsSendTransactionIntegTest() {
        List<String> params = Arrays.asList("aa", "bb", "cc", "dd");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTxRequestContext("set", params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == 0);
        assertTrue(transactionResponse.getResult().length == params.size());
    }

    @Test
    public void getTransactionReceiptTest() throws IOException {
        List<String> params = Arrays.asList("aa", "bb", "cc", "dd");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTxRequestContext("set", params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);
        assertTrue(transactionResponse.getErrorCode() == 0);

        TransactionReceipt receipt = ((BCOSDriver) driver).requestTransactionReceipt(transactionResponse.getHash(), connection);
        assertTrue(receipt.getTransactionHash().equals(transactionResponse.getHash()));
    }

    @Test
    public void getVerifiedTransactionTest() throws IOException {
        List<String> params = Arrays.asList("aa", "bb", "cc", "dd");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTxRequestContext("set", params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);
        assertTrue(transactionResponse.getErrorCode() == 0);

        VerifiedTransaction verifiedTransaction = driver.getVerifiedTransaction(transactionResponse.getHash(), transactionResponse.getBlockNumber(), blockHeaderManager, connection);
        assertEquals(verifiedTransaction.getBlockNumber(), transactionResponse.getBlockNumber());
        assertEquals(verifiedTransaction.getTransactionHash(), transactionResponse.getHash());
        assertTrue(!verifiedTransaction.getRealAddress().isEmpty());

        String[] args = verifiedTransaction.getTransactionRequest().getArgs();
        assertEquals(args.length, params.size());

        TransactionResponse transactionResponse1 = verifiedTransaction.getTransactionResponse();
        assertEquals(transactionResponse1.getHash(), transactionResponse.getHash());
        assertEquals(transactionResponse1.getBlockNumber(), transactionResponse.getBlockNumber());
        assertEquals(transactionResponse1.getResult().length, transactionResponse.getResult().length);
        for (int i=0;i<transactionResponse.getResult().length;++i) {
            assertEquals(transactionResponse.getResult()[i], transactionResponse1.getResult()[i]);
        }
    }
}
