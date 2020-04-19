package com.webank.wecross.stub.bcos;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.web3j.Web3jDefaultConfig;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperCallNotSucStatus;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImplMock;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperTxVerifyMock;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperWithExceptionMock;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperWithNullMock;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.junit.Before;
import org.junit.Test;

public class BCOSDriverTest {

    private Driver driver = null;
    private Account account = null;
    private Connection connection = null;
    private Connection exceptionConnection = null;
    private Connection callNotOkStatusConnection = null;
    private Connection nonExistConnection = null;
    private Connection txVerifyConnection = null;
    private ResourceInfo resourceInfo = null;
    private BlockHeaderManager blockHeaderManager = null;
    private BlockHeaderManager txVerifyBlockHeaderManager = null;

    public TransactionContext<TransactionRequest> createTransactionRequestContext(
            String method, String[] args) {
        TransactionRequest transactionRequest = new TransactionRequest(method, args);
        TransactionContext<TransactionRequest> requestTransactionContext =
                new TransactionContext<TransactionRequest>(
                        transactionRequest, account, resourceInfo, blockHeaderManager);
        requestTransactionContext.setAccount(account);
        requestTransactionContext.setBlockHeaderManager(blockHeaderManager);
        requestTransactionContext.setData(transactionRequest);
        requestTransactionContext.setResourceInfo(resourceInfo);
        return requestTransactionContext;
    }

    public TransactionContext<TransactionRequest> createSendTxTransactionRequestContext(
            String method, String[] args) {
        TransactionRequest transactionRequest = new TransactionRequest(method, args);
        TransactionContext<TransactionRequest> requestTransactionContext =
                new TransactionContext<TransactionRequest>(
                        transactionRequest, account, resourceInfo, blockHeaderManager);
        requestTransactionContext.setAccount(account);
        requestTransactionContext.setBlockHeaderManager(txVerifyBlockHeaderManager);
        requestTransactionContext.setData(transactionRequest);
        requestTransactionContext.setResourceInfo(resourceInfo);
        return requestTransactionContext;
    }

    @Before
    public void initializer() throws Exception {

        BCOSStubFactory bcosStubFactory = new BCOSStubFactory();
        driver = bcosStubFactory.newDriver();
        account = BCOSAccountFactory.build("bcos", "classpath:/accounts/bcos");
        connection =
                BCOSConnectionFactory.build(
                        "./", "stub-sample-ut.toml", new Web3jWrapperImplMock());
        exceptionConnection =
                BCOSConnectionFactory.build(
                        "./", "stub-sample-ut.toml", new Web3jWrapperWithExceptionMock());
        nonExistConnection =
                BCOSConnectionFactory.build(
                        "./", "stub-sample-ut.toml", new Web3jWrapperWithNullMock());
        callNotOkStatusConnection =
                BCOSConnectionFactory.build(
                        "./", "stub-sample-ut.toml", new Web3jWrapperCallNotSucStatus());
        txVerifyConnection =
                BCOSConnectionFactory.build(
                        "./", "stub-sample-ut.toml", new Web3jWrapperTxVerifyMock());
        blockHeaderManager = new BlockHeaderManagerImplMock(new Web3jWrapperImplMock());
        txVerifyBlockHeaderManager = new BlockHeaderManagerImplMock(new Web3jWrapperTxVerifyMock());
        resourceInfo = ((BCOSConnection) connection).getResourceInfoList().get(0);
    }

    @Test
    public void decodeCallTransactionRequestTest() throws Exception {
        String func = "func";
        String[] params = new String[] {"a", "b", "c"};

        TransactionRequest request = new TransactionRequest(func, params);
        Function function = FunctionUtility.newFunction(func, params);

        TransactionParams transaction =
                new TransactionParams(request, FunctionEncoder.encode(function), "0x0", "0x1");

        byte[] data = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction);
        TransactionContext<TransactionRequest> requestTransactionContext =
                driver.decodeTransactionRequest(data);
        assertEquals(requestTransactionContext.getData().getMethod(), func);
        assertEquals(requestTransactionContext.getData().getArgs().length, params.length);
    }

    @Test
    public void decodeCallTransactionRequestExceptionTest() throws Exception {
        assertTrue(Objects.isNull(driver.decodeTransactionRequest(null)));

        String func = "func";
        String[] params = new String[] {"a", "b", "c"};

        TransactionRequest request = new TransactionRequest(func, params);
        Function function = FunctionUtility.newFunction(func, params);

        TransactionParams transaction0 =
                new TransactionParams(null, FunctionEncoder.encode(function), "0x0", "0x1");

        byte[] data0 = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction0);
        TransactionContext<TransactionRequest> requestTransactionContext0 =
                driver.decodeTransactionRequest(data0);
        assertTrue(requestTransactionContext0 == null);

        TransactionParams transaction1 = new TransactionParams(request, null, "0x0", "0x1");

        byte[] data1 = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction1);
        TransactionContext<TransactionRequest> requestTransactionContext1 =
                driver.decodeTransactionRequest(data1);
        assertTrue(requestTransactionContext1 == null);

        TransactionParams transaction2 = new TransactionParams(request, "0x00", "0x0", "0x1");

        byte[] data2 = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction2);
        TransactionContext<TransactionRequest> requestTransactionContext2 =
                driver.decodeTransactionRequest(data2);
        assertTrue(requestTransactionContext2 == null);
    }

    @Test
    public void decodeSendTransactionTransactionRequestTest() throws Exception {
        String func = "func";
        String[] params = new String[] {"a", "b", "c"};

        TransactionRequest request = new TransactionRequest(func, params);
        Function function = FunctionUtility.newFunction(func, params);
        String signTx =
                SignTransaction.sign(
                        GenCredential.create(),
                        "0x0",
                        BigInteger.valueOf(Web3jDefaultConfig.DEFAULT_CHAIN_ID),
                        BigInteger.valueOf(Web3jDefaultConfig.DEFAULT_GROUP_ID),
                        BigInteger.valueOf(1111),
                        FunctionEncoder.encode(function));

        TransactionParams transaction = new TransactionParams(request, signTx, null, null);

        byte[] data = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction);
        TransactionContext<TransactionRequest> requestTransactionContext =
                driver.decodeTransactionRequest(data);
        assertEquals(requestTransactionContext.getData().getMethod(), func);
        assertEquals(requestTransactionContext.getData().getArgs().length, params.length);
    }

    @Test
    public void isTransactionTest() throws IOException {
        Request request = new Request();
        request.setData(new byte[0]);

        request.setType(BCOSRequestType.CALL);
        assertTrue(driver.isTransaction(request));
        request.setType(BCOSRequestType.SEND_TRANSACTION);
        assertTrue(driver.isTransaction(request));

        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);
        assertFalse(driver.isTransaction(request));
        request.setType(BCOSRequestType.GET_BLOCK_HEADER);
        assertFalse(driver.isTransaction(request));

        request.setType(BCOSRequestType.GET_TRANSACTION_PROOF);
        assertFalse(driver.isTransaction(request));

        request.setType(11111);
        assertFalse(driver.isTransaction(request));
    }

    @Test
    public void decodeBlockHeaderTest() throws IOException {
        assertTrue(Objects.isNull(driver.decodeBlockHeader(new byte[0])));
    }

    @Test
    public void getBlockNumberTest() {
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);

        long blockNumber = driver.getBlockNumber(connection);

        assertEquals(blockNumber, 11111);
    }

    @Test
    public void getBlockNumberFailedTest() {
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);

        long blockNumber = driver.getBlockNumber(exceptionConnection);

        assertTrue(blockNumber < 0);
    }

    @Test
    public void getBlockHeaderTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_HEADER);
        request.setData(BigInteger.valueOf(11111).toByteArray());

        byte[] blockHeader1 = driver.getBlockHeader(1111, connection);
        assertTrue(Objects.nonNull(blockHeader1));

        BlockHeader blockHeader = driver.decodeBlockHeader(blockHeader1);
        assertEquals(
                blockHeader.getHash(),
                "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0");
        assertEquals(
                blockHeader.getPrevHash(),
                "0xed0ef6826277efbc9601dedc1b6ea20067eed219e415e1038f111155b8fc1e24");
        assertEquals(
                blockHeader.getReceiptRoot(),
                "0x2a4433b7611c4b1fae16b873ced1dec9a65b82416e448f58fded002c05a10082");
        assertEquals(
                blockHeader.getStateRoot(),
                "0xce8a92c9311e9e0b77842c86adf8fcf91cbab8fb5daefc85b21f501ca8b1f682");
        assertEquals(blockHeader.getNumber(), 331);
        assertEquals(
                blockHeader.getTransactionRoot(),
                "0x07009a9d655cee91e95dcd1c53d5917a58f80e6e6ac689bae24bd911d75c471c");
    }

    @Test
    public void getBlockHeaderFailedTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_HEADER);
        request.setData(BigInteger.valueOf(11111).toByteArray());

        byte[] blockHeader1 = driver.getBlockHeader(1111, exceptionConnection);
        assertTrue(Objects.isNull(blockHeader1));
    }

    @Test
    public void callTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig", "xxxxx"};

        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, connection);

        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse.getResult().length == params.length);

        for (int i = 0; i < params.length; ++i) {
            assertEquals(params[i], transactionResponse.getResult()[i]);
        }
    }

    @Test
    public void callFailedTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig", "xxxxx"};

        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, exceptionConnection);

        assertEquals(
                transactionResponse.getErrorCode().intValue(),
                BCOSStatusCode.HandleCallRequestFailed);
    }

    @Test
    public void callFailedTest0() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig", "xxxxx"};

        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.call(requestTransactionContext, callNotOkStatusConnection);

        assertEquals(
                transactionResponse.getErrorCode().intValue(), BCOSStatusCode.CallNotSuccessStatus);
    }

    @Test
    public void sendTransactionTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.SEND_TRANSACTION);

        String hash = "0x8b3946912d1133f9fb0722a7b607db2456d468386c2e86b035e81ef91d94eb90";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig", "xxxxx"};

        TransactionContext<TransactionRequest> requestTransactionContext =
                createSendTxTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, txVerifyConnection);

        assertTrue(transactionResponse.getErrorCode() == 0);
        assertEquals(transactionResponse.getHash(), hash);
    }

    @Test
    public void sendTransactionFailedTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.SEND_TRANSACTION);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig", "xxxxx"};

        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, exceptionConnection);

        assertTrue(
                transactionResponse.getErrorCode() == BCOSStatusCode.HandleSendTransactionFailed);
    }

    @Test
    public void sendTransactionFailedTest0() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.SEND_TRANSACTION);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig", "xxxxx"};

        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(funName, params);
        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, nonExistConnection);

        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.TransactionReceiptNotExist);
    }

    @Test
    public void getVerifyTransactionTest() throws IOException {
        String transactionHash =
                "0x8b3946912d1133f9fb0722a7b607db2456d468386c2e86b035e81ef91d94eb90";
        long blockNumber = 9;
        VerifiedTransaction verifiedTransaction =
                driver.getVerifiedTransaction(
                        transactionHash,
                        blockNumber,
                        txVerifyBlockHeaderManager,
                        txVerifyConnection);
        assertEquals(verifiedTransaction.getBlockNumber(), blockNumber);
        assertEquals(verifiedTransaction.getTransactionHash(), transactionHash);
    }

    @Test
    public void getVerifyTransactionExceptionTest() throws IOException {
        String transactionHash =
                "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        long blockNumber = 11111;
        VerifiedTransaction verifiedTransaction =
                driver.getVerifiedTransaction(
                        transactionHash, blockNumber, blockHeaderManager, exceptionConnection);
        assertTrue(Objects.isNull(verifiedTransaction));
    }

    @Test
    public void getVerifyTransactionNotExistTest() throws IOException {
        String transactionHash =
                "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        long blockNumber = 11111;
        VerifiedTransaction verifiedTransaction =
                driver.getVerifiedTransaction(
                        transactionHash, blockNumber, blockHeaderManager, nonExistConnection);
        assertTrue(Objects.isNull(verifiedTransaction));
    }

    public TransactionContext<TransactionRequest> createTransactionRequestContext(
            Account account,
            BlockHeaderManager blockHeaderManager,
            ResourceInfo resourceInfo,
            TransactionRequest transactionRequest) {
        TransactionContext<TransactionRequest> transactionContext =
                new TransactionContext<>(
                        transactionRequest, account, resourceInfo, blockHeaderManager);
        return transactionContext;
    }

    @Test
    public void checkRequestTest() {

        BCOSDriver bcosDriver = (BCOSDriver) driver;
        try {
            TransactionContext<TransactionRequest> requestTransactionContext = null;
            bcosDriver.checkRequest(requestTransactionContext);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("TransactionContext is null"));
        }

        try {
            TransactionContext<TransactionRequest> requestTransactionContext =
                    createTransactionRequestContext(
                            null, blockHeaderManager, resourceInfo, new TransactionRequest());
            bcosDriver.checkRequest(requestTransactionContext);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("Account is null"));
        }

        try {
            TransactionContext<TransactionRequest> requestTransactionContext =
                    createTransactionRequestContext(
                            account, null, resourceInfo, new TransactionRequest());
            bcosDriver.checkRequest(requestTransactionContext);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("BlockHeaderManager is null"));
        }

        try {
            TransactionContext<TransactionRequest> requestTransactionContext =
                    createTransactionRequestContext(
                            account, blockHeaderManager, null, new TransactionRequest());
            bcosDriver.checkRequest(requestTransactionContext);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("ResourceInfo is null"));
        }

        try {
            TransactionContext<TransactionRequest> requestTransactionContext =
                    createTransactionRequestContext(
                            account, blockHeaderManager, resourceInfo, null);
            bcosDriver.checkRequest(requestTransactionContext);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("Data is null"));
        }

        try {
            TransactionContext<TransactionRequest> requestTransactionContext =
                    createTransactionRequestContext(
                            account,
                            blockHeaderManager,
                            resourceInfo,
                            new TransactionRequest(null, null));
            bcosDriver.checkRequest(requestTransactionContext);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("Method is null"));
        }
    }

    @Test
    public void checkPropertiesTest() {

        String name = "HelloWeCross";
        Map<Object, Object> properties = new HashMap<>();

        BCOSDriver bcosDriver = (BCOSDriver) driver;
        try {
            ((BCOSDriver) driver).checkProperties(name, properties);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals(" Not found contract address, resource: " + name));
        }

        try {
            properties.put(name, "0x0");
            ((BCOSDriver) driver).checkProperties(name, properties);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals(" Not found groupId, resource: " + name));
        }

        try {
            properties.put(BCOSConstant.BCOS_RESOURCEINFO_GROUP_ID, 1);
            ((BCOSDriver) driver).checkProperties(name, properties);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals(" Not found chainId, resource: " + name));
        }

        try {
            properties.put(name, 1);
            ((BCOSDriver) driver).checkProperties(name, properties);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().startsWith("errorMessage"));
        }

        try {
            properties.put(BCOSConstant.BCOS_RESOURCEINFO_GROUP_ID, "");
            ((BCOSDriver) driver).checkProperties(name, properties);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().startsWith("errorMessage"));
        }

        try {
            properties.put(BCOSConstant.BCOS_RESOURCEINFO_CHAIN_ID, "");
            ((BCOSDriver) driver).checkProperties(name, properties);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().startsWith("errorMessage"));
        }
    }
}
