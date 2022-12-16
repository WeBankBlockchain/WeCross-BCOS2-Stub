package com.webank.wecross.stub.bcos3;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.bcos3.account.BCOSAccount;
import com.webank.wecross.stub.bcos3.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos3.client.ClientDefaultConfig;
import com.webank.wecross.stub.bcos3.client.ClientWrapperCallNotSucStatus;
import com.webank.wecross.stub.bcos3.client.ClientWrapperImplMock;
import com.webank.wecross.stub.bcos3.client.ClientWrapperTxVerifyMock;
import com.webank.wecross.stub.bcos3.client.ClientWrapperWithExceptionMock;
import com.webank.wecross.stub.bcos3.client.ClientWrapperWithNullMock;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import com.webank.wecross.stub.bcos3.common.BCOSRequestType;
import com.webank.wecross.stub.bcos3.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos3.common.BCOSStubException;
import com.webank.wecross.stub.bcos3.common.ObjectMapperFactory;
import com.webank.wecross.stub.bcos3.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos3.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos3.contract.FunctionUtility;
import com.webank.wecross.stub.bcos3.protocol.request.TransactionParams;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.sdk.jni.utilities.tx.TransactionBuilderJniObj;
import org.fisco.bcos.sdk.v3.codec.abi.FunctionEncoder;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIObject;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIObjectFactory;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractCodecJsonWrapper;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.junit.Before;
import org.junit.Test;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class BCOSDriverTest {

    private Driver driver = null;
    private BCOSAccount account = null;
    private Connection connection = null;
    private Connection exceptionConnection = null;
    private Connection callNotOkStatusConnection = null;
    private Connection nonExistConnection = null;
    private Connection txVerifyConnection = null;
    private ResourceInfo resourceInfo = null;
    private BlockManager blockManager = null;
    private BlockManager txVerifyBlockManager = null;
    private TransactionContext transactionContext = null;
    private CryptoSuite cryptoSuite = null;
    private ABIDefinitionFactory abiDefinitionFactory = null;
    private FunctionEncoder functionEncoder = null;
    private TransactionEncoderService transactionEncoderService = null;

    public TransactionRequest createTransactionRequest(String method, String[] args) {
        TransactionRequest transactionRequest = new TransactionRequest(method, args);
        return transactionRequest;
    }

    @Before
    public void initializer() throws Exception {
        BCOS3EcdsaEvmStubFactory bcosSubFactory = new BCOS3EcdsaEvmStubFactory();
        Path path = Path.decode("a.b.c");
        driver = bcosSubFactory.newDriver();

        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./", "stub-sample-ut.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        cryptoSuite = bcosSubFactory.getCryptoSuite();
        abiDefinitionFactory = new ABIDefinitionFactory(cryptoSuite);
        functionEncoder = new FunctionEncoder(cryptoSuite);
        transactionEncoderService = new TransactionEncoderService(cryptoSuite);
        account =
                BCOSAccountFactory.getInstance(cryptoSuite)
                        .build("bcos", "classpath:/accounts/bcos");

        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("tmpBCOSConn-"));
        connection =
                BCOSConnectionFactory.build(
                        bcosStubConfig, new ClientWrapperImplMock(), scheduledExecutorService);
        connection
                .getProperties()
                .put(
                        "VERIFIER",
                        "{\"chainType\":\"BCOS3_ECDSA_EVM\",\"pubKey\":["
                                + "\"ffa9aa23918afcfa5c20a07177e83731c46f153b3ce33b98cb3c4b61c767d06296ef9c1b7f7c6737c3077a6ec61c1a86d665475629cecd1c209b3f9a3b8688dc\","
                                + "\"97af395f31cd52868162c790c2248e23f65c85a64cd0581d323515f6afffc0138279292a55f7bd706f8f1602f142b12a3407a45334eb0cf7daeb064dcec69369\"]}");

        connection.getProperties().put(BCOSConstant.BCOS_STUB_TYPE, "BCOS3_ECDSA_EVM");
        exceptionConnection =
                BCOSConnectionFactory.build(bcosStubConfig, new ClientWrapperWithExceptionMock());
        nonExistConnection =
                BCOSConnectionFactory.build(bcosStubConfig, new ClientWrapperWithNullMock());
        callNotOkStatusConnection =
                BCOSConnectionFactory.build(bcosStubConfig, new ClientWrapperCallNotSucStatus());
        txVerifyConnection =
                BCOSConnectionFactory.build(bcosStubConfig, new ClientWrapperTxVerifyMock());

        blockManager = new BlockManagerImplMock(new ClientWrapperImplMock());
        txVerifyBlockManager = new BlockManagerImplMock(new ClientWrapperTxVerifyMock());
        resourceInfo = ((BCOSConnection) connection).getResourceInfoList().get(0);

        transactionContext = new TransactionContext(account, path, resourceInfo, blockManager);
    }

    @Test
    public void decodeCallTransactionRequestTest() throws Exception {
        String func = "func";
        String[] params = new String[] {"a", "b", "c"};

        TransactionRequest request = new TransactionRequest(func, params);
        Function function = FunctionUtility.newDefaultFunction(func, params);

        TransactionParams transaction =
                new TransactionParams(
                        request,
                        Hex.toHexString(functionEncoder.encode(function)),
                        TransactionParams.SUB_TYPE.CALL);

        byte[] data = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction);

        Pair<Boolean, TransactionRequest> booleanTransactionRequestPair =
                driver.decodeTransactionRequest(Request.newRequest(BCOSRequestType.CALL, data));
        assertTrue(booleanTransactionRequestPair.getKey());
        assertEquals(booleanTransactionRequestPair.getValue().getMethod(), func);
        assertEquals(booleanTransactionRequestPair.getValue().getArgs().length, params.length);
    }

    @Test
    public void decodeSendTransactionTransactionRequestTest() throws Exception {
        String func = "func";
        String[] params = new String[] {"a", "b", "c"};

        TransactionRequest request = new TransactionRequest(func, params);
        Function function = FunctionUtility.newDefaultFunction(func, params);

        long transactionData =
                TransactionBuilderJniObj.createTransactionData(
                        ClientDefaultConfig.DEFAULT_GROUP_ID,
                        ClientDefaultConfig.DEFAULT_CHAIN_ID,
                        "0x0",
                        Hex.toHexString(functionEncoder.encode(function)),
                        "",
                        1111);
        String encodeTransactionData =
                TransactionBuilderJniObj.encodeTransactionData(transactionData);
        TransactionBuilderJniObj.destroyTransactionData(transactionData);

        TransactionParams transaction =
                new TransactionParams(
                        request, encodeTransactionData, TransactionParams.SUB_TYPE.SEND_TX);

        byte[] data = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction);

        Pair<Boolean, TransactionRequest> booleanTransactionRequestPair =
                driver.decodeTransactionRequest(
                        Request.newRequest(BCOSRequestType.SEND_TRANSACTION, data));
        assertTrue(booleanTransactionRequestPair.getKey());
        assertEquals(booleanTransactionRequestPair.getValue().getMethod(), func);
        assertEquals(booleanTransactionRequestPair.getValue().getArgs().length, params.length);
    }

    @Test
    public void decodeProxySendTransactionTransactionRequestTest() throws Exception {
        String func = "set";
        String[] params = new String[] {"a"};

        String abi =
                "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}]";

        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
        ABIDefinition abiDefinition = contractABIDefinition.getFunctions().get("set").get(0);
        ABIObject inputObject = ABIObjectFactory.createInputObject(abiDefinition);
        ContractCodecJsonWrapper abiCodecJsonWrapper = new ContractCodecJsonWrapper();
        ABIObject encoded = abiCodecJsonWrapper.encode(inputObject, Arrays.asList(params));

        TransactionRequest request = new TransactionRequest(func, params);
        Function function =
                FunctionUtility.newSendTransactionProxyFunction(
                        "1", "1", 1, "a.b.Hello", "set(string)", encoded.encode(false));

        long transactionData =
                TransactionBuilderJniObj.createTransactionData(
                        ClientDefaultConfig.DEFAULT_GROUP_ID,
                        ClientDefaultConfig.DEFAULT_CHAIN_ID,
                        "0x0",
                        Hex.toHexString(functionEncoder.encode(function)),
                        "",
                        1111);
        String encodeTransactionData =
                TransactionBuilderJniObj.encodeTransactionData(transactionData);
        TransactionBuilderJniObj.destroyTransactionData(transactionData);

        TransactionParams transaction =
                new TransactionParams(
                        request,
                        encodeTransactionData,
                        TransactionParams.SUB_TYPE.SEND_TX_BY_PROXY);
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

        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(abi);
        ABIDefinition abiDefinition = contractABIDefinition.getFunctions().get("set").get(0);
        ABIObject inputObject = ABIObjectFactory.createInputObject(abiDefinition);
        ContractCodecJsonWrapper abiCodecJsonWrapper = new ContractCodecJsonWrapper();
        ABIObject encoded = abiCodecJsonWrapper.encode(inputObject, Arrays.asList(params));

        Function function =
                FunctionUtility.newConstantCallProxyFunction(
                        "1", "a.b.Hello", "set(string)", encoded.encode(false));

        TransactionRequest request = new TransactionRequest(func, params);

        TransactionParams transaction =
                new TransactionParams(
                        request,
                        Hex.toHexString(functionEncoder.encode(function)),
                        TransactionParams.SUB_TYPE.CALL_BY_PROXY);
        transaction.setAbi(abi);

        byte[] data = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(transaction);
        Pair<Boolean, TransactionRequest> booleanTransactionRequestPair =
                driver.decodeTransactionRequest(Request.newRequest(BCOSRequestType.CALL, data));
        assertTrue(booleanTransactionRequestPair.getKey());
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
                            "0xc403e7f3255c7822e86075c1b97c4de359a511030794af8f8c74692e1b494e03");
                    assertEquals(
                            blockHeader.getPrevHash(),
                            "0x84a1387f18dc03ee329050715819566d8962225b8cee25ce5db5f2d863f3ec3a");
                    assertEquals(
                            blockHeader.getReceiptRoot(),
                            "0x121775bcc0ef53db7fd984d012d7a855990bd873f493564e5de0b3b43745e297");
                    assertEquals(
                            blockHeader.getStateRoot(),
                            "0x71eb54a4996de36cb36ba52136f9a2e87f6c40627b3ab9c87ca9fb641073f013");
                    assertEquals(
                            blockHeader.getTransactionRoot(),
                            "0xaddb42e1db5ef2625c610506b46c745d2418263463b5beb537cf5c10e8d387dd");
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
                            "0xc403e7f3255c7822e86075c1b97c4de359a511030794af8f8c74692e1b494e03");
                    assertEquals(
                            blockHeader.getPrevHash(),
                            "0x84a1387f18dc03ee329050715819566d8962225b8cee25ce5db5f2d863f3ec3a");
                    assertEquals(
                            blockHeader.getReceiptRoot(),
                            "0x121775bcc0ef53db7fd984d012d7a855990bd873f493564e5de0b3b43745e297");
                    assertEquals(
                            blockHeader.getStateRoot(),
                            "0x71eb54a4996de36cb36ba52136f9a2e87f6c40627b3ab9c87ca9fb641073f013");
                    assertEquals(
                            blockHeader.getTransactionRoot(),
                            "0xaddb42e1db5ef2625c610506b46c745d2418263463b5beb537cf5c10e8d387dd");
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
                (transactionException, transactionResponse) -> {
                    assertTrue(Objects.isNull(transactionResponse));
                    asyncToSync.getSemaphore().release();
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
                    assertEquals(
                            verifiedTransaction.getTransactionResponse().getBlockNumber(),
                            blockNumber);
                    assertEquals(
                            verifiedTransaction.getTransactionResponse().getHash(),
                            transactionHash);
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
            bcosDriver.checkTransactionRequest(transactionContext, null);
        } catch (BCOSStubException e) {
            assertEquals((int) e.getErrorCode(), BCOSStatusCode.InvalidParameter);
            assertEquals("TransactionRequest is null", e.getMessage());
        }

        try {
            bcosDriver.checkTransactionRequest(null, null);
        } catch (BCOSStubException e) {
            assertEquals((int) e.getErrorCode(), BCOSStatusCode.InvalidParameter);
            assertEquals("TransactionContext is null", e.getMessage());
        }

        try {
            TransactionContext transactionContext =
                    createTransactionContext(null, blockManager, resourceInfo);
            bcosDriver.checkTransactionRequest(transactionContext, new TransactionRequest());
        } catch (BCOSStubException e) {
            assertEquals((int) e.getErrorCode(), BCOSStatusCode.InvalidParameter);
            assertEquals("Account is null", e.getMessage());
        }

        try {
            TransactionContext transactionContext =
                    createTransactionContext(account, null, resourceInfo);
            bcosDriver.checkTransactionRequest(transactionContext, new TransactionRequest());
        } catch (BCOSStubException e) {
            assertEquals((int) e.getErrorCode(), BCOSStatusCode.InvalidParameter);
            assertEquals("BlockHeaderManager is null", e.getMessage());
        }

        try {
            TransactionRequest transactionRequest = new TransactionRequest(null, null);
            bcosDriver.checkTransactionRequest(transactionContext, transactionRequest);
        } catch (BCOSStubException e) {
            assertEquals((int) e.getErrorCode(), BCOSStatusCode.InvalidParameter);
            assertEquals("Method is null", e.getMessage());
        }
    }
}
