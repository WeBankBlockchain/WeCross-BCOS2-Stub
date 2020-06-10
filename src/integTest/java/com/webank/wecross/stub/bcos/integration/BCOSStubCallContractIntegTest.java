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
        driver.asyncGetBlockNumber(connection, (e, blockNumber) -> assertTrue(blockNumber > 0));
    }

    @Test
    public void getBlockHeaderIntegTest() {
        driver.asyncGetBlockNumber(connection, (e1, blockNumber) -> {
            assertTrue(blockNumber > 0);

            driver.asyncGetBlockHeader(blockNumber, connection, (e2, bytesBlockHeader) -> {
                assertTrue(bytesBlockHeader.length > 0);

                BlockHeader blockHeader = driver.decodeBlockHeader(bytesBlockHeader);
                assertTrue(Objects.nonNull(blockHeader));
                assertTrue(Objects.nonNull(blockHeader.getHash()));
                assertTrue(Objects.nonNull(blockHeader.getReceiptRoot()));
                assertTrue(Objects.nonNull(blockHeader.getTransactionRoot()));
                assertTrue(Objects.nonNull(blockHeader.getPrevHash()));
                assertTrue(Objects.nonNull(blockHeader.getStateRoot()));
                assertTrue(blockHeader.getNumber() == blockNumber);
            });
        });
    }

    @Test
    public void getBlockHeaderFailedIntegTest() {
        driver.asyncGetBlockNumber(connection, (e1, blockNumber) -> {
            assertTrue(blockNumber > 0);

            driver.asyncGetBlockHeader(blockNumber + 1, connection, (e2, bytesBlockHeader) -> {
                assertTrue(Objects.isNull(bytesBlockHeader));
            });
        });

    }

    @Test
    public void callIntegTest() {
        String[] params = null;
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("get", params);
        TransactionResponse transactionResponse =
                null;
        try {
            transactionResponse = driver.call(requestTransactionContext, connection);
        } catch (TransactionException e) {
            // e.printStackTrace();
        }

        assertTrue(Objects.nonNull(transactionResponse));
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse.getResult().length == 0);
    }

    @Test
    public void callNotExistMethodIntegTest() {
        String[] params = new String[]{"aa", "bb", "cc", "dd"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("getNotExist", params);
        TransactionResponse transactionResponse =
                null;
        try {
            transactionResponse = driver.call(requestTransactionContext, connection);
        } catch (TransactionException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.HandleCallRequestFailed);
        }

        assertTrue(Objects.isNull(transactionResponse));
    }

    @Test
    public void sendTransactionIntegTest() {
        String[] params = new String[]{"aa", "bb", "cc", "dd"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("set", params);
        TransactionResponse transactionResponse =
                null;
        try {
            transactionResponse = driver.sendTransaction(requestTransactionContext, connection);
        } catch (TransactionException e) {
        }

        assertTrue(Objects.nonNull(transactionResponse));
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse.getBlockNumber() > 0);
        assertTrue(transactionResponse.getResult().length == params.length);
        for (int i=0;i<transactionResponse.getResult().length;++i) {
            assertEquals(transactionResponse.getResult()[i], params[i]);
        }

        TransactionContext<TransactionRequest> requestTransactionContext0 =
                createTransactionRequestContext("get", null);
        TransactionResponse transactionResponse0 =
                null;
        try {
            transactionResponse0 = driver.call(requestTransactionContext0, connection);
        } catch (TransactionException e) {
        }

        assertTrue(Objects.nonNull(transactionResponse0));
        assertTrue(transactionResponse0.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse0.getResult().length == params.length);

        TransactionContext<TransactionRequest> getRequestTransactionContext =
                createTransactionRequestContext("get", null);
        TransactionResponse getTransactionResponse =
                null;
        try {
            getTransactionResponse = driver.call(getRequestTransactionContext, connection);
        } catch (TransactionException e) {
            //
        }

        assertTrue(Objects.nonNull(getTransactionResponse));
        assertTrue(getTransactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(getTransactionResponse.getResult().length == params.length);
        for (int i=0;i<getTransactionResponse.getResult().length;++i) {
            assertEquals(getTransactionResponse.getResult()[i], params[i]);
        }
    }

    @Test
    public void sendTransactionNotExistIntegTest() {
        String[] params = new String[] {"aa", "bb", "cc", "dd"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("setNotExist", params);
        TransactionResponse transactionResponse =
                null;
        try {
            transactionResponse = driver.sendTransaction(requestTransactionContext, connection);
        } catch (TransactionException e) {
            // assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.SendTransactionNotSuccessStatus);
        }

        assertTrue(Objects.nonNull(transactionResponse));
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.SendTransactionNotSuccessStatus);
    }

    @Test
    public void emptyParamsSendTransactionIntegTest() {
        String[] params = new String[0];
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext("set", params);
        TransactionResponse transactionResponse =
                null;
        try {
            transactionResponse = driver.sendTransaction(requestTransactionContext, connection);
        } catch (TransactionException e) {
        }

        assertTrue(Objects.nonNull(transactionResponse));
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse.getResult().length == params.length);

        TransactionContext<TransactionRequest> getRequestTransactionContext =
                createTransactionRequestContext("get", null);
        TransactionResponse getTransactionResponse =
                null;
        try {
            getTransactionResponse = driver.call(getRequestTransactionContext, connection);
        } catch (TransactionException e) {
        }

        assertTrue(Objects.nonNull(getTransactionResponse));
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

        driver.asyncGetVerifiedTransaction(transactionResponse.getHash(), transactionResponse.getBlockNumber(), blockHeaderManager, connection, (e, verifiedTransaction) -> {
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
        });
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

        driver.asyncGetVerifiedTransaction(transactionResponse.getHash(), transactionResponse.getBlockNumber(), blockHeaderManager, connection, (e, verifiedTransaction) -> {
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
        });
    }

    @Test
    public void getVerifiedTransactionNotExistTest() {
        String transactionHash = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        driver.asyncGetVerifiedTransaction(transactionHash, 1, blockHeaderManager, connection, (e, verifiedTransaction) -> assertTrue(Objects.isNull(verifiedTransaction)));
    }
}
