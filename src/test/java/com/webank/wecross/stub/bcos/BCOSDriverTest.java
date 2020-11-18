package com.webank.wecross.stub.bcos;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.*;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.bcos.abi.ABICodecJsonWrapper;
import com.webank.wecross.stub.bcos.abi.ABIDefinition;
import com.webank.wecross.stub.bcos.abi.ABIDefinitionFactory;
import com.webank.wecross.stub.bcos.abi.ABIObject;
import com.webank.wecross.stub.bcos.abi.ABIObjectFactory;
import com.webank.wecross.stub.bcos.abi.ContractABIDefinition;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
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
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
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
    private BlockManager blockManager = null;
    private BlockManager txVerifyBlockManager = null;
    private TransactionContext transactionContext = null;

    public TransactionRequest createTransactionRequest(String method, String[] args) {
        TransactionRequest transactionRequest = new TransactionRequest(method, args);
        return transactionRequest;
    }

    @Before
    public void initializer() throws Exception {

        BCOSStubFactory bcosSubFactory = new BCOSStubFactory();
        driver = bcosSubFactory.newDriver();
        account = BCOSAccountFactory.build("bcos", "classpath:/accounts/bcos");

        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./", "stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        connection = BCOSConnectionFactory.build(bcosStubConfig, new Web3jWrapperImplMock());
        exceptionConnection =
                BCOSConnectionFactory.build(bcosStubConfig, new Web3jWrapperWithExceptionMock());
        nonExistConnection =
                BCOSConnectionFactory.build(bcosStubConfig, new Web3jWrapperWithNullMock());
        callNotOkStatusConnection =
                BCOSConnectionFactory.build(bcosStubConfig, new Web3jWrapperCallNotSucStatus());
        txVerifyConnection =
                BCOSConnectionFactory.build(bcosStubConfig, new Web3jWrapperTxVerifyMock());

        blockManager = new BlockManagerImplMock(new Web3jWrapperImplMock());
        txVerifyBlockManager = new BlockManagerImplMock(new Web3jWrapperTxVerifyMock());
        resourceInfo = ((BCOSConnection) connection).getResourceInfoList().get(0);

        transactionContext = new TransactionContext(account, null, resourceInfo, blockManager);
    }

    @Test
    public void decodeCallTransactionRequestTest() throws Exception {
        String func = "func";
        String[] params = new String[] {"a", "b", "c"};

        TransactionRequest request = new TransactionRequest(func, params);
        Function function = FunctionUtility.newDefaultFunction(func, params);

        TransactionParams transaction =
                new TransactionParams(
                        request, FunctionEncoder.encode(function), TransactionParams.SUB_TYPE.CALL);

        byte[] data = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction);

        Pair<Boolean, TransactionRequest> booleanTransactionRequestPair =
                driver.decodeTransactionRequest(Request.newRequest(BCOSRequestType.CALL, data));
        assertTrue(booleanTransactionRequestPair.getKey() == true);
        assertEquals(booleanTransactionRequestPair.getValue().getMethod(), func);
        assertEquals(booleanTransactionRequestPair.getValue().getArgs().length, params.length);
    }

    @Test
    public void decodeSendTransactionTransactionRequestTest() throws Exception {
        String func = "func";
        String[] params = new String[] {"a", "b", "c"};

        TransactionRequest request = new TransactionRequest(func, params);
        Function function = FunctionUtility.newDefaultFunction(func, params);
        String signTx =
                SignTransaction.sign(
                        GenCredential.create(),
                        "0x0",
                        BigInteger.valueOf(Web3jDefaultConfig.DEFAULT_CHAIN_ID),
                        BigInteger.valueOf(Web3jDefaultConfig.DEFAULT_GROUP_ID),
                        BigInteger.valueOf(1111),
                        FunctionEncoder.encode(function));

        TransactionParams transaction =
                new TransactionParams(request, signTx, TransactionParams.SUB_TYPE.SEND_TX);

        byte[] data = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction);

        Pair<Boolean, TransactionRequest> booleanTransactionRequestPair =
                driver.decodeTransactionRequest(
                        Request.newRequest(BCOSRequestType.SEND_TRANSACTION, data));
        assertTrue(booleanTransactionRequestPair.getKey() == true);
        assertEquals(booleanTransactionRequestPair.getValue().getMethod(), func);
        assertEquals(booleanTransactionRequestPair.getValue().getArgs().length, params.length);
    }

    @Test
    public void decodeProxySendTransactionTransactionRequestTest() throws Exception {
        String func = "set";
        String[] params = new String[] {"a"};

        String abi =
                "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

        ContractABIDefinition contractABIDefinition = ABIDefinitionFactory.loadABI(abi);
        ABIDefinition abiDefinition = contractABIDefinition.getFunctions().get("set").get(0);
        ABIObject inputObject = ABIObjectFactory.createInputObject(abiDefinition);
        ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
        ABIObject encoded = abiCodecJsonWrapper.encode(inputObject, Arrays.asList(params));

        TransactionRequest request = new TransactionRequest(func, params);
        Function function =
                FunctionUtility.newSendTransactionProxyFunction(
                        "1", "1", 1, "a.b.Hello", "set(string)", encoded.encode());
        String signTx =
                SignTransaction.sign(
                        GenCredential.create(),
                        "0x0",
                        BigInteger.valueOf(Web3jDefaultConfig.DEFAULT_CHAIN_ID),
                        BigInteger.valueOf(Web3jDefaultConfig.DEFAULT_GROUP_ID),
                        BigInteger.valueOf(1111),
                        FunctionEncoder.encode(function));

        TransactionParams transaction =
                new TransactionParams(request, signTx, TransactionParams.SUB_TYPE.SEND_TX_BY_PROXY);
        transaction.setAbi(abi);

        byte[] data = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction);
        Pair<Boolean, TransactionRequest> booleanTransactionRequestPair =
                driver.decodeTransactionRequest(
                        Request.newRequest(BCOSRequestType.SEND_TRANSACTION, data));
        assertTrue(booleanTransactionRequestPair.getKey());
        assertEquals(booleanTransactionRequestPair.getValue().getMethod(), func);
        assertEquals(booleanTransactionRequestPair.getValue().getArgs().length, params.length);
        assertEquals(booleanTransactionRequestPair.getValue().getArgs()[0], params[0]);
    }

    @Test
    public void decodeProxyCallTransactionRequestTest() throws Exception {
        String func = "set";
        String[] params = new String[] {"a"};

        String abi =
                "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

        ContractABIDefinition contractABIDefinition = ABIDefinitionFactory.loadABI(abi);
        ABIDefinition abiDefinition = contractABIDefinition.getFunctions().get("set").get(0);
        ABIObject inputObject = ABIObjectFactory.createInputObject(abiDefinition);
        ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();
        ABIObject encoded = abiCodecJsonWrapper.encode(inputObject, Arrays.asList(params));

        Function function =
                FunctionUtility.newConstantCallProxyFunction(
                        "1", "a.b.Hello", "set(string)", encoded.encode());

        TransactionRequest request = new TransactionRequest(func, params);

        TransactionParams transaction =
                new TransactionParams(
                        request,
                        FunctionEncoder.encode(function),
                        TransactionParams.SUB_TYPE.CALL_BY_PROXY);
        transaction.setAbi(abi);

        byte[] data = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction);
        Pair<Boolean, TransactionRequest> booleanTransactionRequestPair =
                driver.decodeTransactionRequest(Request.newRequest(BCOSRequestType.CALL, data));
        assertTrue(booleanTransactionRequestPair.getKey() == true);
        assertEquals(booleanTransactionRequestPair.getValue().getMethod(), func);
        assertEquals(booleanTransactionRequestPair.getValue().getArgs().length, params.length);
        assertEquals(booleanTransactionRequestPair.getValue().getArgs()[0], params[0]);
    }

    @Test
    public void getBlockNumberTest() {
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);

        driver.asyncGetBlockNumber(
                connection, (e, blockNumber) -> assertEquals(blockNumber, 11111));
    }

    @Test
    public void getBlockNumberFailedTest() {
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);

        driver.asyncGetBlockNumber(exceptionConnection, (e, blockNumber) -> assertNotNull(e));
    }

    @Test
    public void getBlockHeaderTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_BY_NUMBER);
        request.setData(BigInteger.valueOf(11111).toByteArray());

        driver.asyncGetBlock(
                1111,
                false,
                connection,
                (e, block) -> {
                    assertTrue(Objects.isNull(e));
                    assertTrue(Objects.nonNull(block));
                    assertTrue(block.getRawBytes().length > 1);
                    assertTrue(!block.getTransactionsHashes().isEmpty());

                    BlockHeader blockHeader = block.getBlockHeader();
                    assertEquals(
                            blockHeader.getHash(),
                            "0x99576e7567d258bd6426ddaf953ec0c953778b2f09a078423103c6555aa4362d");
                    assertEquals(
                            blockHeader.getPrevHash(),
                            "0x64ba7bf5c6b5a83854774475bf8511d5e9bb38d8a962a859b52aa9c9fba0c685");
                    assertEquals(
                            blockHeader.getReceiptRoot(),
                            "0x049389563053748a0fd2b256260b9e8c76a427b543bee18f3a221d80d1553da8");
                    assertEquals(
                            blockHeader.getStateRoot(),
                            "0xce8a92c9311e9e0b77842c86adf8fcf91cbab8fb5daefc85b21f501ca8b1f682");
                    assertEquals(
                            blockHeader.getTransactionRoot(),
                            "0xb563f70188512a085b5607cac0c35480336a566de736c83410a062c9acc785ad");
                });
    }

    @Test
    public void getBlockTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_BY_NUMBER);
        request.setData(BigInteger.valueOf(11111).toByteArray());

        driver.asyncGetBlock(
                1111,
                false,
                connection,
                (e, block) -> {
                    assertTrue(Objects.isNull(e));
                    assertTrue(Objects.nonNull(block));
                    assertTrue(block.getRawBytes().length > 1);
                    assertFalse(block.getTransactionsHashes().isEmpty());

                    BlockHeader blockHeader = block.getBlockHeader();
                    assertTrue(!block.transactionsHashes.isEmpty());
                    assertEquals(
                            blockHeader.getHash(),
                            "0x99576e7567d258bd6426ddaf953ec0c953778b2f09a078423103c6555aa4362d");
                    assertEquals(
                            blockHeader.getPrevHash(),
                            "0x64ba7bf5c6b5a83854774475bf8511d5e9bb38d8a962a859b52aa9c9fba0c685");
                    assertEquals(
                            blockHeader.getReceiptRoot(),
                            "0x049389563053748a0fd2b256260b9e8c76a427b543bee18f3a221d80d1553da8");
                    assertEquals(
                            blockHeader.getStateRoot(),
                            "0xce8a92c9311e9e0b77842c86adf8fcf91cbab8fb5daefc85b21f501ca8b1f682");
                    assertEquals(
                            blockHeader.getTransactionRoot(),
                            "0xb563f70188512a085b5607cac0c35480336a566de736c83410a062c9acc785ad");
                });
    }

    @Test
    public void getBlockHeaderFailedTest() throws IOException {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_BY_NUMBER);
        request.setData(BigInteger.valueOf(11111).toByteArray());

        driver.asyncGetBlock(
                1111, false, exceptionConnection, (e, block) -> assertTrue(Objects.isNull(block)));
    }

    @Test
    public void callTest() throws Exception {

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig", "xxxxx"};
        BCOSConnection bcosConnection = (BCOSConnection) connection;
        bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_NAME, address);

        TransactionRequest transactionRequest = createTransactionRequest(funName, params);
        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncCall(
                transactionContext,
                transactionRequest,
                false,
                connection,
                new Driver.Callback() {
                    @Override
                    public void onTransactionResponse(
                            TransactionException transactionException,
                            TransactionResponse transactionResponse) {
                        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
                        assertTrue(transactionResponse.getResult().length == params.length);

                        for (int i = 0; i < params.length; ++i) {
                            assertEquals(params[i], transactionResponse.getResult()[i]);
                        }
                        asyncToSync.getSemaphore().release();
                    }
                });
        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void callFailedTest() throws InterruptedException {

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig", "xxxxx"};

        TransactionRequest transactionRequest = createTransactionRequest(funName, params);

        AsyncToSync asyncToSync = new AsyncToSync();

        driver.asyncCall(
                transactionContext,
                transactionRequest,
                false,
                exceptionConnection,
                new Driver.Callback() {
                    @Override
                    public void onTransactionResponse(
                            TransactionException transactionException,
                            TransactionResponse transactionResponse) {
                        assertTrue(Objects.isNull(transactionResponse));
                        asyncToSync.getSemaphore().release();
                    }
                });

        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void callFailedTest0() throws Exception {

        Request request = new Request();
        request.setType(BCOSRequestType.CALL);

        String address = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        String funName = "funcName";
        String[] params = new String[] {"abc", "def", "hig", "xxxxx"};
        BCOSConnection bcosConnection = (BCOSConnection) callNotOkStatusConnection;
        bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_NAME, "0x0");

        TransactionRequest transactionRequest = createTransactionRequest(funName, params);
        AsyncToSync asyncToSync = new AsyncToSync();

        driver.asyncCall(
                transactionContext,
                transactionRequest,
                false,
                callNotOkStatusConnection,
                new Driver.Callback() {
                    @Override
                    public void onTransactionResponse(
                            TransactionException transactionException,
                            TransactionResponse transactionResponse) {
                        assertEquals(
                                transactionResponse.getErrorCode().intValue(),
                                BCOSStatusCode.CallNotSuccessStatus);
                        asyncToSync.getSemaphore().release();
                    }
                });
        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void getVerifyTransactionTest() throws Exception {
        String transactionHash =
                "0x8b3946912d1133f9fb0722a7b607db2456d468386c2e86b035e81ef91d94eb90";
        long blockNumber = 9;
        driver.asyncGetTransaction(
                transactionHash,
                blockNumber,
                txVerifyBlockManager,
                true,
                txVerifyConnection,
                (e, verifiedTransaction) -> {
                    assertEquals(verifiedTransaction.getBlockNumber(), blockNumber);
                    assertEquals(verifiedTransaction.getTxHash(), transactionHash);
                });
    }

    @Test
    public void getVerifyTransactionExceptionTest() throws Exception {
        String transactionHash =
                "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        long blockNumber = 11111;
        driver.asyncGetTransaction(
                transactionHash,
                blockNumber,
                blockManager,
                true,
                exceptionConnection,
                (e, verifiedTransaction) -> assertTrue(Objects.isNull(verifiedTransaction)));
    }

    @Test
    public void getVerifyTransactionNotExistTest() throws Exception {
        String transactionHash =
                "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        long blockNumber = 11111;
        driver.asyncGetTransaction(
                transactionHash,
                blockNumber,
                blockManager,
                true,
                nonExistConnection,
                (e, verifiedTransaction) -> assertTrue(Objects.isNull(verifiedTransaction)));
    }

    public TransactionContext createTransactionContext(
            Account account, BlockManager blockManager, ResourceInfo resourceInfo) {
        TransactionContext transactionContext =
                new TransactionContext(account, null, resourceInfo, blockManager);
        return transactionContext;
    }

    @Test
    public void checkRequestTest() {

        BCOSDriver bcosDriver = (BCOSDriver) driver;
        try {
            bcosDriver.checkRequest(transactionContext, null);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("TransactionRequest is null"));
        }

        try {
            bcosDriver.checkRequest(null, null);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("TransactionContext is null"));
        }

        try {
            TransactionContext transactionContext =
                    createTransactionContext(null, blockManager, resourceInfo);
            bcosDriver.checkRequest(transactionContext, new TransactionRequest());
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("Account is null"));
        }

        try {
            TransactionContext transactionContext =
                    createTransactionContext(account, null, resourceInfo);
            bcosDriver.checkRequest(transactionContext, new TransactionRequest());
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("BlockHeaderManager is null"));
        }

        try {
            TransactionRequest transactionRequest = new TransactionRequest(null, null);
            bcosDriver.checkRequest(transactionContext, transactionRequest);
        } catch (BCOSStubException e) {
            assertTrue(e.getErrorCode().intValue() == BCOSStatusCode.InvalidParameter);
            assertTrue(e.getMessage().equals("Method is null"));
        }
    }
}
