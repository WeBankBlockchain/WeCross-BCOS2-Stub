package com.webank.wecross.stub.bcos.integration;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.AsyncCnsService;
import com.webank.wecross.stub.bcos.AsyncToSync;
import com.webank.wecross.stub.bcos.BCOSBaseStubFactory;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSConnectionFactory;
import com.webank.wecross.stub.bcos.BCOSDriver;
import com.webank.wecross.stub.bcos.BCOSGMStubFactory;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.custom.DeployContractHandler;
import com.webank.wecross.stub.bcos.performance.hellowecross.HelloWeCross;
import com.webank.wecross.stub.bcos.preparation.ProxyContract;
import com.webank.wecross.stub.bcos.web3j.AbstractWeb3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jBlockManager;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImplV26;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.fisco.bcos.web3j.utils.Numeric;
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
    private BlockManager blockManager = null;
    private ConnectionEventHandlerImplMock connectionEventHandlerImplMock = new ConnectionEventHandlerImplMock();

    private AsyncCnsService asyncCnsService = null;

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

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public void setBlockManager(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    public TransactionRequest createTransactionRequest(
            Path path, String method, String[] args) {
        return new TransactionRequest(method, args);
    }

    public TransactionContext createTransactionContext(
            Path path) {
                return new TransactionContext( account, path, resourceInfo, blockManager);
    }

    @Before
    public void initializer() throws Exception {

        System.setProperty("jdk.tls.namedGroups", "secp256k1");

        /** load stub.toml config */
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./chains/bcos/", "stub.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();
        String type = bcosStubConfig.getType();
        logger.info(" === >> initial type:  {}", type);

        BCOSBaseStubFactory stubFactory = type.toLowerCase().startsWith("gm")? new BCOSGMStubFactory() : new BCOSStubFactory();

        driver = stubFactory.newDriver();
        account = stubFactory.newAccount("IntegBCOSAccount", "classpath:/accounts/bcos");
        connection = BCOSConnectionFactory.build("./chains/bcos/", "stub.toml");
        connection.setConnectionEventHandler(connectionEventHandlerImplMock);

        AbstractWeb3jWrapper web3jWrapper = ((BCOSConnection) connection).getWeb3jWrapper();

        BCOSAccount bcosAccount = (BCOSAccount) account;
        blockManager = new Web3jBlockManager(web3jWrapper);
        asyncCnsService = ((BCOSDriver) driver).getAsyncCnsService();

        helloWeCross =
                HelloWeCross
                        .deploy(
                                web3jWrapper.getWeb3j(),
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
        File file =
                resolver.getResource("classpath:solidity/WeCrossProxy.sol")
                        .getFile();

        ProxyContract proxyContract = new ProxyContract();
        proxyContract.setAccount((BCOSAccount) account);
        proxyContract.setConnection((BCOSConnection)connection);
        CnsInfo cnsInfo = proxyContract.deployContractAndRegisterCNS(file, "WeCrossProxy", "WeCrossProxy", String.valueOf(System.currentTimeMillis()));
        connection.getProperties().put(BCOSConstant.BCOS_PROXY_NAME, cnsInfo.getAddress());
        connection.getProperties().put(BCOSConstant.BCOS_PROXY_ABI, cnsInfo.getAbi());
    }

    @Test
    public void deployContractTxGetTest() throws InterruptedException {
        Optional<TransactionReceipt> transactionReceipt = helloWeCross.getTransactionReceipt();
        AsyncToSync asyncToSync = new AsyncToSync();

        driver.asyncGetTransaction(transactionReceipt.get().getTransactionHash(), transactionReceipt.get().getBlockNumber().longValue(), blockManager, true, connection, (e, transaction) -> {
            assertTrue(Objects.nonNull(transaction));
            assertTrue(Objects.isNull(e));
            assertEquals(account.getIdentity(), transaction.getAccountIdentity());
            assertFalse(transaction.isTransactionByProxy());
            assertTrue(transaction.getReceiptBytes().length > 1);
            assertTrue(transaction.getTxBytes().length > 1);
            asyncToSync.getSemaphore().release();
        });
        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void deployContractByProxyTest() throws Exception {
        String[] params = new String[4] ;

        params[0] = "a.b.HelloWeCross";
        params[1] = "1.1" + System.currentTimeMillis();
        params[2] = Base64.getEncoder().encodeToString(Numeric.hexStringToByteArray(HelloWeCross.BINARY));
        params[3] = HelloWeCross.ABI;

        Path path = Path.decode("a.b.WeCrossProxy");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, BCOSConstant.PROXY_METHOD_DEPLOY, params);

        TransactionContext transactionContext = createTransactionContext(path);

        AtomicReference<String> addr = new AtomicReference<>("");
        AsyncToSync asyncToSync = new AsyncToSync();

        driver.asyncSendTransaction(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 1);
            assertTrue(res.getResult()[0].length() == 42);
            addr.set(res.getResult()[0]);
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);
    }

    @Test
    public void getBlockNumberIntegIntegTest() throws InterruptedException {
        AsyncToSync asyncToSync = new AsyncToSync();

        driver.asyncGetBlockNumber(connection, (e, blockNumber) -> { asyncToSync.getSemaphore().release(); assertTrue(blockNumber > 0); });

        asyncToSync.semaphore.acquire(1);
    }

    @Test
    public void getBlockHeaderIntegTest() throws InterruptedException {
        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncGetBlockNumber(connection, (e1, blockNumber) -> {
            assertTrue(blockNumber > 0);

            driver.asyncGetBlock(blockNumber, false, connection, (e2, block) -> {
                assertNull(e2);
                BlockHeader blockHeader = block.getBlockHeader();
                List<String> transactionsHashes = block.getTransactionsHashes();
                assertTrue(transactionsHashes.size() == 1);
                assertTrue(Objects.nonNull(transactionsHashes.get(0)));
                assertTrue(block.getRawBytes().length > 1);
                assertTrue(Objects.nonNull(blockHeader));
                assertTrue(Objects.nonNull(blockHeader.getHash()));
                assertTrue(Objects.nonNull(blockHeader.getReceiptRoot()));
                assertTrue(Objects.nonNull(blockHeader.getTransactionRoot()));
                assertTrue(Objects.nonNull(blockHeader.getPrevHash()));
                assertTrue(Objects.nonNull(blockHeader.getStateRoot()));
                assertTrue(blockHeader.getNumber() == blockNumber);
                asyncToSync.getSemaphore().release();
            });
        });
        asyncToSync.semaphore.acquire(1);
    }

    @Test
    public void getBlockHeaderFailedIntegTest() throws InterruptedException {
        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncGetBlockNumber(connection, (e1, blockNumber) -> {
            assertTrue(blockNumber > 0);

            driver.asyncGetBlock(blockNumber + 1, true, connection, (e2, bytesBlockHeader) -> {
                assertTrue(Objects.isNull(bytesBlockHeader));
                asyncToSync.getSemaphore().release();
            });
        });

        asyncToSync.semaphore.acquire(1);
    }

    @Test
    public void callIntegTest() throws Exception {
        Path path = Path.decode("a.b.WeCrossProxy");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "getVersion", null);

        TransactionContext transactionContext = createTransactionContext(path);
        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncCall(transactionContext, transactionRequest, false, connection, new Driver.Callback() {
            @Override
            public void onTransactionResponse(TransactionException transactionException, TransactionResponse transactionResponse) {
                assertNull(transactionException);
                assertNotNull(transactionResponse);
                assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
                assertTrue(transactionResponse.getResult().length != 0);
                asyncToSync.getSemaphore().release();
            }
        });

        asyncToSync.semaphore.acquire(1);
    }

    @Test
    public void callNotExistMethodIntegTest() throws Exception {
        Path path = Path.decode("a.b.HelloWorld");
        String[] params = new String[]{"a.b.1", "a.b.2"};
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "addPaths", params);
        TransactionContext transactionContext = createTransactionContext(path);
        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncCall(transactionContext, transactionRequest, false, connection, new Driver.Callback() {
                @Override
                public void onTransactionResponse(TransactionException transactionException, TransactionResponse transactionResponse) {
                    assertTrue(Objects.nonNull(transactionException));
                    assertTrue(Objects.isNull(transactionResponse));
                    asyncToSync.getSemaphore().release();
                }
            });

        asyncToSync.semaphore.acquire(1);
    }

    @Test
    public void sendTransactionIntegTest() throws Exception {
        Path path = Path.decode("a.b.WeCrossProxy");
        String[] params = new String[]{"a.b.c"};
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "addPath", params);
        TransactionContext transactionContext = createTransactionContext(path);

        AsyncToSync asyncToSync = new AsyncToSync();
        final String[] hash = {""};
        final long[] blockNumber = {0};
        driver.asyncSendTransaction(transactionContext, transactionRequest, false, connection, new Driver.Callback() {
            @Override
            public void onTransactionResponse(TransactionException transactionException, TransactionResponse transactionResponse) {
                assertNotNull(transactionResponse);
                assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
                assertTrue(transactionResponse.getBlockNumber() > 0);
                hash[0] = transactionResponse.getHash();
                blockNumber[0] = transactionResponse.getBlockNumber();
                asyncToSync.getSemaphore().release();
            }
        });

        asyncToSync.getSemaphore().acquire();

        AsyncToSync asyncToSync3 = new AsyncToSync();
        driver.asyncGetTransaction(hash[0], blockNumber[0], blockManager, true, connection, (e, transaction) -> {
            assertTrue(Objects.nonNull(transaction));
            assertTrue(Objects.isNull(e));
            assertTrue(transaction.isTransactionByProxy());
            assertTrue(transaction.getReceiptBytes().length > 1);
            assertTrue(transaction.getTxBytes().length > 1);
            asyncToSync3.getSemaphore().release();
        });
        asyncToSync3.getSemaphore().acquire();

        TransactionRequest transactionRequest1 =
                createTransactionRequest(path, "getPaths", new String[]{});

        AsyncToSync asyncToSync1 = new AsyncToSync();
        driver.asyncCall(transactionContext, transactionRequest1, false, connection, new Driver.Callback() {
            @Override
            public void onTransactionResponse(TransactionException transactionException, TransactionResponse transactionResponse) {
                assertNull(transactionException);
                assertNotNull(transactionResponse);
                assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
                assertTrue(transactionResponse.getResult().length == params.length);
                asyncToSync1.getSemaphore().release();
            }
        });
        asyncToSync1.getSemaphore().acquire();


        TransactionRequest transactionRequest2 =
                createTransactionRequest(path, "getPaths", new String[]{});
        AsyncToSync asyncToSync2 = new AsyncToSync();
        driver.asyncCall(transactionContext, transactionRequest2, false, connection, new Driver.Callback() {
            @Override
            public void onTransactionResponse(TransactionException transactionException, TransactionResponse transactionResponse) {
                assertNotNull(transactionResponse);
                assertTrue(transactionResponse.getErrorCode() == BCOSStatusCode.Success);
                assertTrue(transactionResponse.getResult().length == params.length);
                for (int i = 0; i < transactionResponse.getResult().length; ++i) {
                    assertTrue(Objects.nonNull(transactionResponse.getResult()[i]));
                }
                asyncToSync2.getSemaphore().release();
            }
        });
        asyncToSync2.getSemaphore().acquire();
    }

    @Test
    public void sendTransactionNotExistIntegTest() throws Exception {
        Path path = Path.decode("a.b.HelloWorld");
        String[] params = new String[]{"aa", "bb", "cc", "dd"};
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "setNotExist", params);

        TransactionContext transactionContext = createTransactionContext(path);
        AsyncToSync asyncToSync = new AsyncToSync();
        final String[] hash = {""};
        final long[] blockNumber = {0};
        driver.asyncSendTransaction(transactionContext, transactionRequest, true, connection, new Driver.Callback() {
            @Override
            public void onTransactionResponse(TransactionException transactionException, TransactionResponse transactionResponse) {
                assertNotNull(transactionException);
                assertTrue(transactionException.getErrorCode() == BCOSStatusCode.MethodNotExist);
                asyncToSync.getSemaphore().release();
            }
        });
        asyncToSync.getSemaphore().acquire();


        AsyncToSync asyncToSync1 = new AsyncToSync();
        driver.asyncGetTransaction(hash[0], blockNumber[0], blockManager, true, connection, (e, transaction) -> {
            assertTrue(Objects.isNull(transaction));
            assertTrue(Objects.nonNull(e));
            BCOSStubException e1 = (BCOSStubException)e;
            assertTrue(e1.getErrorCode() == BCOSStatusCode.TransactionReceiptProofNotExist || e1.getErrorCode() == BCOSStatusCode.TransactionNotExist);
            asyncToSync1.getSemaphore().release();
        });
        asyncToSync1.getSemaphore().acquire();
    }

    @Test
    public void getVerifiedTransactionNotExistTest() throws Exception {
        AsyncToSync asyncToSync = new AsyncToSync();
        String transactionHash = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";
        driver.asyncGetTransaction(transactionHash, 1, blockManager,true, connection, (e, verifiedTransaction) -> {
            assertTrue(Objects.nonNull(e));
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

        String constructorParams = "constructor params";
        Object[] args =
                new Object[]{
                        "HelloWorld",
                        new String(contractBytes),
                        "HelloWorld",
                        String.valueOf(System.currentTimeMillis()),
                        constructorParams
                };


        AsyncToSync asyncToSync = new AsyncToSync();

        DeployContractHandler commandHandler = new DeployContractHandler();
        commandHandler.setAsyncCnsService(asyncCnsService);

        commandHandler.handle(Path.decode("a.b.HelloWorld"),
                args,
                account,
                blockManager,
                connection,
                (error, response) -> {
            assertNull(error);
            assertNotNull(response);
            assertTrue(((String)response).length() == 42);
            asyncToSync.getSemaphore().release();
        });
        asyncToSync.getSemaphore().acquire();

        assertTrue(Objects.nonNull(asyncCnsService.getAbiCache().get("HelloWorld")));
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

        String params1 = "1";
        String params2 = "[1,2,3]";
        String params3 = "HelloWorld";
        Object[] args =
                new Object[]{
                        "TupleTest", new String(contractBytes), "TupleTest", String.valueOf(System.currentTimeMillis()), params1, params2, params3
                };


        AsyncToSync asyncToSync = new AsyncToSync();

        DeployContractHandler commandHandler = new DeployContractHandler();
        commandHandler.setAsyncCnsService(asyncCnsService);

        commandHandler.handle(Path.decode("a.b.TupleTest"), args, account, blockManager, connection, (error, response) -> {
            assertNull(error);
            assertNotNull(response);
            assertTrue(((String)response).length() == 42);
            asyncToSync.getSemaphore().release();
        });
        asyncToSync.getSemaphore().acquire();

        assertTrue(Objects.nonNull(asyncCnsService.getAbiCache().get("TupleTest")));
    }

    @Test
    public void cnsServiceTest() throws InterruptedException {
        AsyncToSync asyncToSync = new AsyncToSync();
        asyncCnsService.selectByName(BCOSConstant.BCOS_PROXY_NAME, connection, driver, (exception, infoList) -> {
            Assert.assertTrue(Objects.isNull(exception));
            Assert.assertTrue(!Objects.isNull(infoList));
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void cnsServiceLoopTest() throws Exception {
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();
        String path =
                resolver.getResource("classpath:solidity/HelloWorld.sol")
                        .getFile()
                        .getAbsolutePath();

        File file = new File(path);
        byte[] contractBytes;
        contractBytes = Files.readAllBytes(file.toPath());

        DeployContractHandler commandHandler = new DeployContractHandler();
        commandHandler.setAsyncCnsService(asyncCnsService);

        for (int i = 0; i < 3; i++) {
            String constructorParams = "constructor params";
            String baseName = "HelloWorld";
            Object[] args =
                    new Object[]{
                            baseName + i,
                            new String(contractBytes),
                            "HelloWorld",
                            String.valueOf(System.currentTimeMillis()),
                            constructorParams
                    };

            AsyncToSync asyncToSync = new AsyncToSync();
            commandHandler.handle(Path.decode("a.b." + baseName + i),
                    args,
                    account,
                    blockManager,
                    connection,
                    (error, response) -> {
                        assertNull(error);
                        assertNotNull(response);
                        assertTrue(((String)response).length() == 42);
                        asyncToSync.getSemaphore().release();
                    });
            asyncToSync.getSemaphore().acquire();
            assertTrue(Objects.nonNull(asyncCnsService.getAbiCache().get(baseName + i)));
        }
    }

    @Test
    public void callByProxyTest() throws Exception {
        String[] params = new String[]{"hello", "world"};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "get2", params);

        TransactionContext transactionContext = createTransactionContext(path);

        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncCall(transactionContext,  transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 1);
            assertTrue(res.getResult()[0].equals(params[0] + params[1]));
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void sendTransactionGet1ByProxyTest() throws Exception {
        String[] params = new String[]{"hello world"};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "get1", params);

        TransactionContext transactionContext = createTransactionContext(path);

        AtomicReference<String> hash = new AtomicReference<>("");
        AsyncToSync asyncToSync = new AsyncToSync();
        final long[] blockNumber = {0};
        driver.asyncSendTransaction(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            hash.set(res.getHash());
            blockNumber[0] = res.getBlockNumber();
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);

        AsyncToSync asyncToSync1 = new AsyncToSync();
        driver.asyncGetTransaction(hash.get(), blockNumber[0], blockManager, true, connection, (e, transaction) -> {
            assertTrue(Objects.isNull(e));
            assertTrue(transaction.getTransactionResponse().getHash().equals(hash.get()));
            assertTrue(transaction.isTransactionByProxy());
            assertEquals("0", (String)transaction.getTransactionRequest().getOptions().get(StubConstant.XA_TRANSACTION_ID));
            assertEquals(account.getIdentity(), transaction.getAccountIdentity());
            assertEquals(transaction.getResource(), path.getResource());
            assertEquals(0, (long)transaction.getTransactionRequest().getOptions().get(StubConstant.XA_TRANSACTION_SEQ));
            assertTrue(transaction.getTransactionRequest().getMethod().equals("get1"));
            assertEquals(transaction.getTransactionRequest().getArgs()[0], params[0]);
            assertEquals(transaction.getTransactionResponse().getErrorCode().intValue(), 0);
            assertEquals(transaction.getTransactionResponse().getResult()[0], params[0]);
            asyncToSync1.getSemaphore().release();
        });

        asyncToSync1.semaphore.acquire(1);
    }

    @Test
    public void sendTransactionGet2ByProxyTest() throws Exception {
        String[] params = new String[]{"hello", "world"};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "get2", params);

        TransactionContext transactionContext = createTransactionContext(path);

        AsyncToSync asyncToSync = new AsyncToSync();
        AtomicReference<String> hash = new AtomicReference<>("");
        final long[] blockNumber = {0};
        driver.asyncSendTransaction(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 1);
            assertTrue(res.getResult()[0].equals(params[0] + params[1]));
            hash.set(res.getHash());
            blockNumber[0] = res.getBlockNumber();
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);

        AsyncToSync asyncToSync1 = new AsyncToSync();
        driver.asyncGetTransaction(hash.get(), blockNumber[0], blockManager, true, connection, (exception, res) -> {
            assertTrue(Objects.isNull(exception));
            assertTrue(res.getTransactionResponse().getHash().equals(hash.get()));
            assertTrue(res.isTransactionByProxy());
            assertEquals(res.getResource(), path.getResource());
            assertEquals("0", (String)res.getTransactionRequest().getOptions().get(StubConstant.XA_TRANSACTION_ID));
            assertTrue(res.getTransactionRequest().getMethod().equals("get2"));
            assertEquals(res.getTransactionRequest().getArgs()[0], params[0]);
            assertEquals(res.getTransactionRequest().getArgs()[1], params[1]);
            assertEquals(res.getTransactionResponse().getErrorCode().intValue(), 0);
            assertEquals(res.getTransactionResponse().getResult()[0], params[0] + params[1]);
            asyncToSync1.getSemaphore().release();
        });

        asyncToSync1.semaphore.acquire(1);
    }

    @Test
    public void sendTransactionSetByProxyTest() throws Exception {
        String[] params = new String[]{"hello"};
        Path path = Path.decode("a.b.HelloWorld");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "set", params);

        TransactionContext transactionContext = createTransactionContext(path);

        AtomicReference<String> hash = new AtomicReference<>("");
        AsyncToSync asyncToSync = new AsyncToSync();
        final long[] blockNumber = {0};
        driver.asyncSendTransaction(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 0);
            hash.set(res.getHash());
            blockNumber[0] = res.getBlockNumber();
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);

        AsyncToSync asyncToSync1 = new AsyncToSync();
        driver.asyncGetTransaction(hash.get(), blockNumber[0], blockManager, true, connection, (exception, transaction) -> {
            assertTrue(Objects.isNull(exception));
            assertTrue(transaction.getTransactionResponse().getHash().equals(hash.get()));
            assertTrue(transaction.isTransactionByProxy());
            assertEquals(transaction.getResource(), path.getResource());
            assertEquals(0, (long)transaction.getTransactionRequest().getOptions().get(StubConstant.XA_TRANSACTION_SEQ));
            assertTrue(transaction.getTransactionRequest().getMethod().equals("set"));
            assertEquals(transaction.getTransactionRequest().getArgs()[0], params[0]);
            assertEquals(transaction.getTransactionResponse().getErrorCode().intValue(), 0);
            asyncToSync1.getSemaphore().release();
        });

        asyncToSync1.semaphore.acquire(1);
    }

    @Test
    public void callByProxyOnTupleTest() throws Exception {
        String[] params = new String[]{};
        Path path = Path.decode("a.b.TupleTest");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "get1", params);
        TransactionContext transactionContext = createTransactionContext(path);

        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncCall(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 3);
            assertTrue(res.getResult()[0].equals("1"));
            assertTrue(res.getResult()[1].equals("[ 1, 2, 3 ]"));
            assertTrue(res.getResult()[2].equals("HelloWorld"));
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void callByProxyOnTupleTestGetAndSet() throws Exception {
        String[] params = new String[]{"1111", "[ 22222, 33333, 44444 ]", "55555"};
        Path path = Path.decode("a.b.TupleTest");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "getAndSet1", params);

        TransactionContext transactionContext = createTransactionContext(path);

        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncCall(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 3);
            assertTrue(res.getResult()[0].equals("1111"));
            assertTrue(res.getResult()[1].equals("[ 22222, 33333, 44444 ]"));
            assertTrue(res.getResult()[2].equals("55555"));
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void callByProxyOnTupleTestGetSampleTupleValue() throws Exception {
        String[] params = new String[]{};
        Path path = Path.decode("a.b.TupleTest");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, "getSampleTupleValue", params);

        TransactionContext transactionContext = createTransactionContext(path);

        AsyncToSync asyncToSync = new AsyncToSync();
        driver.asyncCall(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertTrue(res.getErrorCode() == BCOSStatusCode.Success);
            assertTrue(res.getResult().length == 3);
            assertTrue(res.getResult()[0].equals("100"));
            assertTrue(res.getResult()[1].equals("[ [ [ \"Hello world! + 1 \", 100, [ [ 1, 2, 3 ] ] ] ], [ [ \"Hello world! + 2 \", 101, [ [ 4, 5, 6 ] ] ] ] ]"));
            assertTrue(res.getResult()[2].equals("Hello world! + 3 "));
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.getSemaphore().acquire();
    }
}
