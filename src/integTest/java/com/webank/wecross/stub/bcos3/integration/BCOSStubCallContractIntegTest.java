package com.webank.wecross.stub.bcos3.integration;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.bcos3.AsyncBfsService;
import com.webank.wecross.stub.bcos3.AsyncToSync;
import com.webank.wecross.stub.bcos3.BCOSBaseStubFactory;
import com.webank.wecross.stub.bcos3.BCOSConnection;
import com.webank.wecross.stub.bcos3.BCOSConnectionFactory;
import com.webank.wecross.stub.bcos3.BCOSDriver;
import com.webank.wecross.stub.bcos3.account.BCOSAccount;
import com.webank.wecross.stub.bcos3.client.AbstractClientWrapper;
import com.webank.wecross.stub.bcos3.client.ClientBlockManager;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import com.webank.wecross.stub.bcos3.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos3.common.BCOSStubException;
import com.webank.wecross.stub.bcos3.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos3.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos3.custom.CommandHandler;
import com.webank.wecross.stub.bcos3.custom.DeployContractHandler;
import com.webank.wecross.stub.bcos3.custom.DeployContractWasmHandler;
import com.webank.wecross.stub.bcos3.performance.hellowecross.HelloWeCross;
import com.webank.wecross.stub.bcos3.preparation.ProxyContract;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractCodecJsonWrapper;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSInfo;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class BCOSStubCallContractIntegTest {

    private static final Logger logger = LoggerFactory.getLogger(BCOSStubCallContractIntegTest.class);

    private HelloWeCross helloWeCross = null;
    private Driver driver = null;
    private Account account = null;
    private Connection connection = null;
    private ResourceInfo resourceInfo = null;
    private BlockManager blockManager = null;
    private boolean isWasm;
    private ConnectionEventHandlerImplMock connectionEventHandlerImplMock = new ConnectionEventHandlerImplMock();
    private CryptoSuite cryptoSuite = null;
    private AsyncBfsService asyncBfsService = null;


    @Before
    public void initializer() throws Exception {
        System.setProperty("jdk.tls.namedGroups", "SM2,secp256k1,x25519,secp256r1,secp384r1,secp521r1,x448");

        /** load stub.toml config */
        connection = BCOSConnectionFactory.build("./chains/bcos/", "stub.toml");

        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser("./chains/bcos/", "stub.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        isWasm = bcosStubConfig.isWASMStub();
        boolean isGMStub = bcosStubConfig.isGMStub();
        int cryptoType = isGMStub ? CryptoType.SM_TYPE : CryptoType.ECDSA_TYPE;
        String alg = isGMStub ? BCOSConstant.SM2P256V1 : BCOSConstant.SECP256K1;
        String stubType = bcosStubConfig.getType();
        BCOSBaseStubFactory stubFactory = new BCOSBaseStubFactory(cryptoType, alg, stubType);

        driver = stubFactory.newDriver();
        account = stubFactory.newAccount("IntegBCOSAccount", isGMStub ? "classpath:/accounts/gm_bcos" : "classpath:/accounts/bcos");

        connection.setConnectionEventHandler(connectionEventHandlerImplMock);

        AbstractClientWrapper clientWrapper = ((BCOSConnection) connection).getClientWrapper();

        BCOSAccount bcosAccount = (BCOSAccount) account;
        blockManager = new ClientBlockManager(clientWrapper);
        asyncBfsService = ((BCOSDriver) driver).getAsyncBfsService();
        cryptoSuite = clientWrapper.getCryptoSuite();

        logger.info(" === >> initial type is is Wasm:  {}", isWasm);

        helloWeCross =
                HelloWeCross
                        .deploy(
                                clientWrapper.getClient(),
                                bcosAccount.getCredentials());

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
        proxyContract.setConnection((BCOSConnection) connection);
        BFSInfo bfsInfo = proxyContract.deployContractAndLinkBFS(file, "WeCrossProxy", "WeCrossProxy");
        connection.getProperties().put(BCOSConstant.BCOS_PROXY_NAME, bfsInfo.getAddress());
        connection.getProperties().put(BCOSConstant.BCOS_PROXY_ABI, bfsInfo.getAbi());
    }

    @Test
    public void deployContractTxGetTest() throws InterruptedException {
        TransactionReceipt transactionReceipt = helloWeCross.getDeployReceipt();
        AsyncToSync asyncToSync = new AsyncToSync();

        driver.asyncGetTransaction(transactionReceipt.getTransactionHash(), transactionReceipt.getBlockNumber().longValue(), blockManager, true, connection, (e, transaction) -> {
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
        String[] params = new String[3];

        params[0] = "a.b.HelloWeCross";
        params[1] = ContractCodecJsonWrapper.HexEncodedDataPrefix + HelloWeCross.BINARY;
        params[2] = HelloWeCross.ABI;

        Path path = Path.decode("a.b.WeCrossProxy");
        TransactionRequest transactionRequest =
                createTransactionRequest(path, BCOSConstant.PROXY_METHOD_DEPLOY, params);

        TransactionContext transactionContext = createTransactionContext(path);

        AtomicReference<String> addr = new AtomicReference<>("");
        AsyncToSync asyncToSync = new AsyncToSync();

        driver.asyncSendTransaction(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertEquals((int) res.getErrorCode(), BCOSStatusCode.Success);
            assertEquals(1, res.getResult().length);
            assertEquals(42, res.getResult()[0].length());
            addr.set(res.getResult()[0]);
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);
    }

    @Test
    public void getBlockNumberIntegIntegTest() throws InterruptedException {
        AsyncToSync asyncToSync = new AsyncToSync();

        driver.asyncGetBlockNumber(connection, (e, blockNumber) -> {
            asyncToSync.getSemaphore().release();
            assertTrue(blockNumber > 0);
        });

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
                assertEquals(1, transactionsHashes.size());
                assertTrue(Objects.nonNull(transactionsHashes.get(0)));
                assertTrue(block.getRawBytes().length > 1);
                assertTrue(Objects.nonNull(blockHeader));
                assertTrue(Objects.nonNull(blockHeader.getHash()));
                assertTrue(Objects.nonNull(blockHeader.getReceiptRoot()));
                assertTrue(Objects.nonNull(blockHeader.getTransactionRoot()));
                assertTrue(Objects.nonNull(blockHeader.getPrevHash()));
                assertTrue(Objects.nonNull(blockHeader.getStateRoot()));
                assertEquals(blockHeader.getNumber(), blockNumber);
                asyncToSync.getSemaphore().release();
            });
        });
        asyncToSync.semaphore.acquire(1);
    }

    @Test
    public void getGenesisBlockIntegTest() throws InterruptedException {
        AsyncToSync asyncToSync = new AsyncToSync();


        driver.asyncGetBlock(0, true, connection, (e2, block) -> {
            assertNull(e2);
            BlockHeader blockHeader = block.getBlockHeader();
            assertTrue(block.getRawBytes().length > 1);
            assertTrue(Objects.nonNull(blockHeader));
            assertTrue(Objects.nonNull(blockHeader.getHash()));
            assertEquals(0, blockHeader.getNumber());
            asyncToSync.getSemaphore().release();
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
        driver.asyncCall(transactionContext, transactionRequest, false, connection, (transactionException, transactionResponse) -> {
            assertNull(transactionException);
            assertNotNull(transactionResponse);
            assertEquals((int) transactionResponse.getErrorCode(), BCOSStatusCode.Success);
            assertTrue(transactionResponse.getResult().length != 0);
            asyncToSync.getSemaphore().release();
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
        driver.asyncCall(transactionContext, transactionRequest, false, connection, (transactionException, transactionResponse) -> {
            assertTrue(Objects.nonNull(transactionException));
            assertTrue(Objects.isNull(transactionResponse));
            asyncToSync.getSemaphore().release();
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
        driver.asyncSendTransaction(transactionContext, transactionRequest, false, connection, (transactionException, transactionResponse) -> {
            assertNotNull(transactionResponse);
            assertEquals((int) transactionResponse.getErrorCode(), BCOSStatusCode.Success);
            assertTrue(transactionResponse.getBlockNumber() > 0);
            hash[0] = transactionResponse.getHash();
            blockNumber[0] = transactionResponse.getBlockNumber();
            asyncToSync.getSemaphore().release();
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
        driver.asyncCall(transactionContext, transactionRequest1, false, connection, (transactionException, transactionResponse) -> {
            assertNull(transactionException);
            assertNotNull(transactionResponse);
            assertEquals((int) transactionResponse.getErrorCode(), BCOSStatusCode.Success);
            assertEquals(transactionResponse.getResult().length, params.length);
            asyncToSync1.getSemaphore().release();
        });
        asyncToSync1.getSemaphore().acquire();


        TransactionRequest transactionRequest2 =
                createTransactionRequest(path, "getPaths", new String[]{});
        AsyncToSync asyncToSync2 = new AsyncToSync();
        driver.asyncCall(transactionContext, transactionRequest2, false, connection, (transactionException, transactionResponse) -> {
            assertNotNull(transactionResponse);
            assertEquals((int) transactionResponse.getErrorCode(), BCOSStatusCode.Success);
            assertEquals(transactionResponse.getResult().length, params.length);
            for (int i = 0; i < transactionResponse.getResult().length; ++i) {
                assertTrue(Objects.nonNull(transactionResponse.getResult()[i]));
            }
            asyncToSync2.getSemaphore().release();
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
        driver.asyncSendTransaction(transactionContext, transactionRequest, true, connection, (transactionException, transactionResponse) -> {
            assertNotNull(transactionException);
            assertEquals((int) transactionException.getErrorCode(), BCOSStatusCode.MethodNotExist);
            asyncToSync.getSemaphore().release();
        });
        asyncToSync.getSemaphore().acquire();


        AsyncToSync asyncToSync1 = new AsyncToSync();

        driver.asyncGetTransaction(hash[0], blockNumber[0], blockManager, true, connection, (e, transaction) -> {
            assertTrue(Objects.isNull(transaction));
            assertTrue(Objects.nonNull(e));
            BCOSStubException e1 = (BCOSStubException) e;
            assertTrue(e1.getErrorCode() == BCOSStatusCode.TransactionReceiptProofNotExist || e1.getErrorCode() == BCOSStatusCode.TransactionNotExist);
            asyncToSync1.getSemaphore().release();
        });
        asyncToSync1.getSemaphore().acquire();
    }

    @Test
    public void getVerifiedTransactionNotExistTest() throws Exception {
        AsyncToSync asyncToSync = new AsyncToSync();
        String transactionHash = "0x6db416c8ac6b1fe7ed08771de419b71c084ee5969029346806324601f2e3f0d0";

        driver.asyncGetTransaction(transactionHash, 1, blockManager, true, connection, (e, verifiedTransaction) -> {
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

        CommandHandler commandHandler;
        if (isWasm) {
            commandHandler = new DeployContractWasmHandler();
            ((DeployContractWasmHandler) commandHandler).setAsyncBfsService(asyncBfsService);
        } else {
            commandHandler = new DeployContractHandler();
            ((DeployContractHandler) commandHandler).setAsyncBfsService(asyncBfsService);
        }

        commandHandler.handle(Path.decode("a.b.HelloWorld"),
                args,
                account,
                blockManager,
                connection,
                (error, response) -> {
                    assertNull(error);
                    assertNotNull(response);
                    assertEquals(42, ((String) response).length());
                    asyncToSync.getSemaphore().release();
                }, cryptoSuite);
        asyncToSync.getSemaphore().acquire();

        assertTrue(Objects.nonNull(asyncBfsService.getAbiCache().get("HelloWorld")));
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

        CommandHandler commandHandler;
        if (isWasm) {
            commandHandler = new DeployContractWasmHandler();
            ((DeployContractWasmHandler) commandHandler).setAsyncBfsService(asyncBfsService);
        } else {
            commandHandler = new DeployContractHandler();
            ((DeployContractHandler) commandHandler).setAsyncBfsService(asyncBfsService);
        }

        commandHandler.handle(Path.decode("a.b.TupleTest"), args, account, blockManager, connection, (error, response) -> {
            assertNull(error);
            assertNotNull(response);
            assertEquals(42, ((String) response).length());
            asyncToSync.getSemaphore().release();
        }, cryptoSuite);
        asyncToSync.getSemaphore().acquire();

        assertTrue(Objects.nonNull(asyncBfsService.getAbiCache().get("TupleTest")));
    }

    @Test
    public void bfsServiceTest() throws InterruptedException {
        AsyncToSync asyncToSync = new AsyncToSync();
        asyncBfsService.readlink(BCOSConstant.BCOS_PROXY_NAME, connection, driver, (exception, infoList) -> {
            Assert.assertTrue(Objects.isNull(exception));
            Assert.assertFalse(Objects.isNull(infoList));
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.getSemaphore().acquire();
    }

    @Test
    public void bfsServiceLoopTest() throws Exception {
        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();
        String path =
                resolver.getResource("classpath:solidity/HelloWorld.sol")
                        .getFile()
                        .getAbsolutePath();

        File file = new File(path);
        byte[] contractBytes;
        contractBytes = Files.readAllBytes(file.toPath());

        CommandHandler commandHandler;
        if (isWasm) {
            commandHandler = new DeployContractWasmHandler();
            ((DeployContractWasmHandler) commandHandler).setAsyncBfsService(asyncBfsService);
        } else {
            commandHandler = new DeployContractHandler();
            ((DeployContractHandler) commandHandler).setAsyncBfsService(asyncBfsService);
        }
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
                        assertEquals(42, ((String) response).length());
                        asyncToSync.getSemaphore().release();
                    }, cryptoSuite);
            asyncToSync.getSemaphore().acquire();
            assertTrue(Objects.nonNull(asyncBfsService.getAbiCache().get(baseName + i)));
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
        driver.asyncCall(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertEquals((int) res.getErrorCode(), BCOSStatusCode.Success);
            assertEquals(1, res.getResult().length);
            assertEquals(res.getResult()[0], params[0] + params[1]);
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
            assertEquals((int) res.getErrorCode(), BCOSStatusCode.Success);
            hash.set(res.getHash());
            blockNumber[0] = res.getBlockNumber();
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);

        AsyncToSync asyncToSync1 = new AsyncToSync();

        driver.asyncGetTransaction(hash.get(), blockNumber[0], blockManager, true, connection, (e, transaction) -> {
            assertTrue(Objects.isNull(e));
            assertEquals(transaction.getTransactionResponse().getHash(), hash.get());
            assertTrue(transaction.isTransactionByProxy());
            assertEquals("0", (String) transaction.getTransactionRequest().getOptions().get(StubConstant.XA_TRANSACTION_ID));
            assertEquals(account.getIdentity(), transaction.getAccountIdentity());
            assertEquals(transaction.getResource(), path.getResource());
            assertEquals(0, (long) transaction.getTransactionRequest().getOptions().get(StubConstant.XA_TRANSACTION_SEQ));
            assertEquals("get1", transaction.getTransactionRequest().getMethod());
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
            assertEquals((int) res.getErrorCode(), BCOSStatusCode.Success);
            assertEquals(1, res.getResult().length);
            assertEquals(res.getResult()[0], params[0] + params[1]);
            hash.set(res.getHash());
            blockNumber[0] = res.getBlockNumber();
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);

        AsyncToSync asyncToSync1 = new AsyncToSync();

        driver.asyncGetTransaction(hash.get(), blockNumber[0], blockManager, true, connection, (exception, res) -> {
            assertTrue(Objects.isNull(exception));
            assertEquals(res.getTransactionResponse().getHash(), hash.get());
            assertTrue(res.isTransactionByProxy());
            assertEquals(res.getResource(), path.getResource());
            assertEquals("0", (String) res.getTransactionRequest().getOptions().get(StubConstant.XA_TRANSACTION_ID));
            assertEquals("get2", res.getTransactionRequest().getMethod());
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
            assertEquals((int) res.getErrorCode(), BCOSStatusCode.Success);
            assertEquals(0, res.getResult().length);
            hash.set(res.getHash());
            blockNumber[0] = res.getBlockNumber();
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.semaphore.acquire(1);

        AsyncToSync asyncToSync1 = new AsyncToSync();

        driver.asyncGetTransaction(hash.get(), blockNumber[0], blockManager, true, connection, (exception, transaction) -> {
            assertTrue(Objects.isNull(exception));
            assertEquals(transaction.getTransactionResponse().getHash(), hash.get());
            assertTrue(transaction.isTransactionByProxy());
            assertEquals(transaction.getResource(), path.getResource());
            assertEquals(0, (long) transaction.getTransactionRequest().getOptions().get(StubConstant.XA_TRANSACTION_SEQ));
            assertEquals("set", transaction.getTransactionRequest().getMethod());
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
            assertEquals((int) res.getErrorCode(), BCOSStatusCode.Success);
            assertEquals(3, res.getResult().length);
            assertEquals("1", res.getResult()[0]);
            assertEquals("[ 1, 2, 3 ]", res.getResult()[1]);
            assertEquals("HelloWorld", res.getResult()[2]);
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
        //todo asyncCall  asyncSendTransaction
        driver.asyncSendTransaction(transactionContext, transactionRequest, true, connection, (exception, res) -> {
            assertTrue(Objects.nonNull(res));
            assertEquals((int) res.getErrorCode(), BCOSStatusCode.Success);
            assertEquals(3, res.getResult().length);
            assertEquals("1111", res.getResult()[0]);
            assertEquals("[ 22222, 33333, 44444 ]", res.getResult()[1]);
            assertEquals("55555", res.getResult()[2]);
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
            assertEquals((int) res.getErrorCode(), BCOSStatusCode.Success);
            assertEquals(3, res.getResult().length);
            assertEquals("100", res.getResult()[0]);
            assertEquals("[ [ [ \"Hello world! + 1 \", 100, [ [ 1, 2, 3 ] ] ] ], [ [ \"Hello world! + 2 \", 101, [ [ 4, 5, 6 ] ] ] ] ]", res.getResult()[1]);
            assertEquals("Hello world! + 3 ", res.getResult()[2]);
            asyncToSync.getSemaphore().release();
        });

        asyncToSync.getSemaphore().acquire();
    }

    public TransactionRequest createTransactionRequest(
            Path path, String method, String[] args) {
        return new TransactionRequest(method, args);
    }

    public TransactionContext createTransactionContext(
            Path path) {
        return new TransactionContext(account, path, resourceInfo, blockManager);
    }
}
