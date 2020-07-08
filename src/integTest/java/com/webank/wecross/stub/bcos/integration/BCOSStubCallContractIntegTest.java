package com.webank.wecross.stub.bcos.integration;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.AsyncCnsService;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSConnectionFactory;
import com.webank.wecross.stub.bcos.BCOSDriver;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.custom.CommandHandler;
import com.webank.wecross.stub.bcos.custom.DeployContractHandler;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImpl;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;

import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class BCOSStubCallContractIntegTest {

    private static final Logger logger =
            LoggerFactory.getLogger(BCOSStubCallContractIntegTest.class);

    private HelloWeCross helloWeCross = null;

    private Driver driver = null;
    private Account account = null;
    private Connection connection = null;
    private ResourceInfo resourceInfo = null;
    private BlockHeaderManager blockHeaderManager = null;
    private ConnectionEventHandlerImplMock connectionEventHandlerImplMock = new ConnectionEventHandlerImplMock();

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
            Path path, String method, String[] args) {
        TransactionRequest transactionRequest =
                new TransactionRequest(method, args);
        transactionRequest.setOptions(new HashMap<>());
        // transactionRequest.setPath(path);
        TransactionContext<TransactionRequest> requestTransactionContext =
                new TransactionContext<>(
                        transactionRequest, account, path, resourceInfo, blockHeaderManager);
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
        connection = BCOSConnectionFactory.build("./chains/bcos/", "stub.toml", null);
        connection.setConnectionEventHandler(connectionEventHandlerImplMock);

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
        deployProxy();
        deployHelloWorldTest();
        deployTupleTestContract();
    }

    private void deployProxy() throws Exception {
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();
        String path =
                resolver.getResource("classpath:solidity/WeCrossProxy.sol")
                        .getFile()
                        .getAbsolutePath();

        File file = new File(path);
        byte[] contractBytes;
        contractBytes = Files.readAllBytes(file.toPath());

        Object[] args =
                new Object[]{
                        "WeCrossProxy", new String(contractBytes), "WeCrossProxy", String.valueOf(System.currentTimeMillis()),
                };

        AsyncToSync asyncToSync = new AsyncToSync();
        CommandHandler commandHandler = new DeployContractHandler();
        commandHandler.handle(Path.decode("a.b.WeCrossProxy"), args, account, blockHeaderManager, connection, new HashMap<>(), (error, response) -> {
            if (Objects.nonNull(error)) {
                error.printStackTrace();
            }
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.getSemaphore().acquire();
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
    public void callIntegTest() throws Exception {
        String[] params = new String[]{};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "getVersion", params);
        TransactionResponse transactionResponse =
                null;
        try {
            transactionResponse = driver.call(requestTransactionContext, connection);
        } catch (TransactionException e) {
            // e.printStackTrace();
        }

        assertTrue(Objects.nonNull(transactionResponse));
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse.getResult().length != 0);
    }

    @Test
    public void callNotExistMethodIntegTest() throws Exception {
        Path path = Path.decode("a.b.HelloWorld");
        String[] params = new String[]{"a.b.1", "a.b.2"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "addPaths", params);
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
    public void sendTransactionIntegTest() throws Exception {
        Path path = Path.decode("a.b.HelloWorld");
        String[] params = new String[]{"a.b.c"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "addPath", params);
        TransactionResponse transactionResponse =
                null;
        try {
            transactionResponse = driver.sendTransaction(requestTransactionContext, connection);
        } catch (TransactionException e) {
        }

        assertTrue(Objects.nonNull(transactionResponse));
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(transactionResponse.getBlockNumber() > 0);
//        assertTrue(transactionResponse.getResult().length == 0);
//        for (int i=0;i<transactionResponse.getResult().length;++i) {
//            assertEquals(transactionResponse.getResult()[i], params[i]);
//        }

        TransactionContext<TransactionRequest> requestTransactionContext0 =
                createTransactionRequestContext(path, "getPaths", new String[]{});
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
                createTransactionRequestContext(path, "getPaths", new String[]{});
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
        for (int i = 0; i < getTransactionResponse.getResult().length; ++i) {
            assertEquals(getTransactionResponse.getResult()[i], params[i]);
        }
    }

    @Test
    public void sendTransactionNotExistIntegTest() throws Exception {
        Path path = Path.decode("a.b.HelloWorld");
        String[] params = new String[]{"aa", "bb", "cc", "dd"};
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "setNotExist", params);
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
    public void getTransactionReceiptTest() throws Exception {
        Path path = Path.decode("a.b.HelloWorld");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "addPath", new String[]{"a.b.c"});
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
    public void getVerifiedTransactionTest() throws Exception {
        String[] params = new String[]{"a.b.c"};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "addPath", params);

        TransactionResponse transactionResponse =
                driver.sendTransaction(requestTransactionContext, connection);
        assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
        assertTrue(Objects.nonNull(transactionResponse.getHash()));

        TransactionProof transactionProof = ((BCOSDriver) driver).requestTransactionProof(transactionResponse.getHash(), connection);
        TransactionReceipt transactionReceipt = transactionProof.getReceiptAndProof().getTransactionReceipt();
        Transaction transaction = transactionProof.getTransAndProof().getTransaction();
        assertEquals(transaction.getHash(), transactionReceipt.getTransactionHash());
        assertEquals(transaction.getBlockNumber().longValue(), transactionReceipt.getBlockNumber().longValue());

        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncGetVerifiedTransaction(path, transactionResponse.getHash(), transactionResponse.getBlockNumber(), blockHeaderManager, connection, (e, verifiedTransaction) -> {
            assertEquals(verifiedTransaction.getBlockNumber(), transactionResponse.getBlockNumber());
            assertEquals(verifiedTransaction.getTransactionHash(), transactionResponse.getHash());
            assertEquals(verifiedTransaction.getRealAddress(), connection.getProperties().get(BCOSConstant.BCOS_PROXY_NAME));

            TransactionRequest transactionRequest = verifiedTransaction.getTransactionRequest();
            assertEquals(transactionRequest.getArgs().length, params.length);

            TransactionResponse transactionResponse1 = verifiedTransaction.getTransactionResponse();
            assertEquals(transactionResponse1.getErrorCode().intValue(), 0);
            assertEquals(transactionResponse1.getHash(), transactionReceipt.getTransactionHash());
            assertEquals(transactionResponse1.getBlockNumber(), transactionReceipt.getBlockNumber().longValue());
            asyncToSync.getSemaphore().release();
        });
        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void getVerifiedTransactionNotExistTest() throws Exception {
        Path path = Path.decode("a.b.HelloWorld");
        AsyncToSync asyncToSync = new AsyncToSync();
        String transactionHash = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        driver.asyncGetVerifiedTransaction(path, transactionHash, 1, blockHeaderManager, connection, (e, verifiedTransaction) -> {
            assertTrue(Objects.isNull(verifiedTransaction));
            asyncToSync.getSemaphore().release();
        });
        asyncToSync.getSemaphore().acquire();
    }

    public void deployHelloWorldTest() throws Exception {
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();
        String path =
                resolver.getResource("classpath:solidity/HelloWorld.sol")
                        .getFile()
                        .getAbsolutePath();

        File file = new File(path);
        byte[] contractBytes;
        contractBytes = Files.readAllBytes(file.toPath());

        Object[] args =
                new Object[]{
                        "HelloWorld", new String(contractBytes), "HelloWorld", String.valueOf(System.currentTimeMillis()),
                };


        AsyncToSync asyncToSync = new AsyncToSync();

        CommandHandler commandHandler = new DeployContractHandler();
        commandHandler.handle(Path.decode("a.b.HelloWorld"), args, account, blockHeaderManager, connection, new HashMap<>(), (error, response) -> {
            assertNull(error);
            assertNotNull(response);
            asyncToSync.getSemaphore().release();
        });
        asyncToSync.getSemaphore().acquire();
    }

    public void deployTupleTestContract() throws Exception {
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();
        String path =
                resolver.getResource("classpath:solidity/TupleTest.sol")
                        .getFile()
                        .getAbsolutePath();

        File file = new File(path);
        byte[] contractBytes;
        contractBytes = Files.readAllBytes(file.toPath());

        Object[] args =
                new Object[]{
                        "TupleTest", new String(contractBytes), "TupleTest", String.valueOf(System.currentTimeMillis()),
                };


        AsyncToSync asyncToSync = new AsyncToSync();

        CommandHandler commandHandler = new DeployContractHandler();
        commandHandler.handle(Path.decode("a.b.TupleTest"), args, account, blockHeaderManager, connection, new HashMap<>(), (error, response) -> {
            assertNull(error);
            assertNotNull(response);
            asyncToSync.getSemaphore().release();
        });
        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void CnsServiceTest() throws InterruptedException {
        AsyncCnsService asyncCnsService = new AsyncCnsService();
        AsyncToSync asyncToSync = new AsyncToSync();
        asyncCnsService.selectByName(BCOSConstant.BCOS_PROXY_NAME, account, connection, (exception, infoList) -> {
            System.out.println(infoList);
            asyncToSync.getSemaphore().release();
            Assert.assertTrue(Objects.isNull(exception));
            Assert.assertTrue(!Objects.isNull(infoList));
        });

        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void CallByProxyTest() throws Exception {
        String[] params = new String[]{"hello", "world"};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "get2", params);

        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncCallByProxy(requestTransactionContext, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 1);
            assertTrue(res.getResult()[0].equals(params[0] + params[1]));
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void sendTransactionByProxyTest() throws Exception {
        String[] params = new String[]{"hello world"};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "get1", params);

        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncSendTransactionByProxy(requestTransactionContext, connection, (exception, res) -> {
            System.out.println("exception: " + exception);
            System.out.println("resp: " + res);
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);
    }


    @Test
    public void sendTransactionByProxyTest0() throws Exception {
        String[] params = new String[]{"hello", "world"};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "get2", params);

        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncSendTransactionByProxy(requestTransactionContext, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 1);
            assertTrue(res.getResult()[0].equals(params[0] + params[1]));
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);
    }

    @Test
    public void sendTransactionByProxyTes1() throws Exception {
        String[] params = new String[]{"hello"};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionContext<TransactionRequest> requestTransactionContext =
                createTransactionRequestContext(path, "set", params);

        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncSendTransactionByProxy(requestTransactionContext, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 0);
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);
    }

}
