package com.webank.wecross.stub.bcos.luyu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.AccountFactory;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.bcos.AsyncCnsService;
import com.webank.wecross.stub.bcos.BCOSDriver;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import link.luyu.protocol.algorithm.ecdsa.secp256k1.EcdsaSecp256k1WithSHA256;
import link.luyu.protocol.algorithm.sm2.SM2WithSM3;
import link.luyu.protocol.link.Driver;
import link.luyu.protocol.network.Account;
import link.luyu.protocol.network.CallRequest;
import link.luyu.protocol.network.CallResponse;
import link.luyu.protocol.network.Events;
import link.luyu.protocol.network.Receipt;
import link.luyu.protocol.network.Resource;
import link.luyu.protocol.network.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.tx.txdecode.LogResult;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuyuDriverAdapter implements Driver {
    private static Logger logger = LoggerFactory.getLogger(LuyuDriverAdapter.class);
    private static final int QUERY_SUCCESS = 0;
    private static final int QUERY_FAILED = 99100;

    private String type;
    private String chainPath;
    private BCOSDriver wecrossDriver;
    private LuyuWeCrossConnection luyuWeCrossConnection;
    private LuyuMemoryBlockManager blockManager;
    private AccountFactory accountFactory;
    private Events routerEventsHandler;
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public LuyuDriverAdapter(
            String type,
            String chainPath,
            com.webank.wecross.stub.Driver wecrossDriver,
            LuyuWeCrossConnection luyuWeCrossConnection,
            LuyuMemoryBlockManager blockManager,
            AccountFactory accountFactory) {
        this.type = type;
        this.chainPath = chainPath;
        this.wecrossDriver = (BCOSDriver) wecrossDriver;
        this.luyuWeCrossConnection = luyuWeCrossConnection;
        this.blockManager = blockManager;
        this.accountFactory = accountFactory;
    }

    @Override
    public void start() throws RuntimeException {
        this.luyuWeCrossConnection
                .getLuyuConnection()
                .subscribe(
                        LuyuDefault.SUBSCRIBE_CHAIN_SEND_TX_EVENT,
                        new byte[0],
                        new link.luyu.protocol.link.Connection.Callback() {
                            @Override
                            public void onResponse(
                                    int errorCode, String message, byte[] responseData) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "Receive sendTransaction event. code: {}, message: {}, data: {}",
                                            errorCode,
                                            message,
                                            new String(responseData));
                                }
                                if (errorCode == 0) {
                                    handleChainSendTransactionEvent(message, responseData);
                                }
                            }
                        });

        this.luyuWeCrossConnection
                .getLuyuConnection()
                .subscribe(
                        LuyuDefault.SUBSCRIBE_CHAIN_CALL_EVENT,
                        new byte[0],
                        new link.luyu.protocol.link.Connection.Callback() {
                            @Override
                            public void onResponse(
                                    int errorCode, String message, byte[] responseData) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "Receive call event. code: {}, message: {}, data: {}",
                                            errorCode,
                                            message,
                                            new String(responseData));
                                }
                                if (errorCode == 0) {
                                    handleChainCallEvent(message, responseData);
                                }
                            }
                        });
    }

    @Override
    public void stop() throws RuntimeException {
        blockManager.stop();
    }

    @Override
    public void sendTransaction(Account account, Transaction request, ReceiptCallback callback) {
        try {

            Path path = Path.decode(request.getPath());
            TransactionContext context =
                    new TransactionContext(toWeCrossAccount(account), path, null, blockManager);
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setMethod(request.getMethod());
            transactionRequest.setArgs(request.getArgs());
            wecrossDriver.asyncSendTransaction(
                    context,
                    transactionRequest,
                    false,
                    luyuWeCrossConnection,
                    new com.webank.wecross.stub.Driver.Callback() {
                        @Override
                        public void onTransactionResponse(
                                TransactionException transactionException,
                                TransactionResponse transactionResponse) {
                            int status = QUERY_SUCCESS;
                            String message = "Success";
                            Receipt receipt = null;
                            if (transactionException != null) {
                                status = transactionException.getErrorCode();
                                message = transactionException.getMessage();
                            }

                            if (transactionResponse != null) {
                                receipt = new Receipt();
                                receipt.setResult(transactionResponse.getResult());
                                receipt.setCode(transactionResponse.getErrorCode());
                                receipt.setMessage(transactionResponse.getMessage());
                                receipt.setPath(request.getPath());
                                receipt.setMethod(request.getMethod());
                                receipt.setArgs(request.getArgs());
                                receipt.setTransactionHash(transactionResponse.getHash());
                                receipt.setTransactionBytes(new byte[] {}); // TODO: Add bytes
                                receipt.setBlockNumber(transactionResponse.getBlockNumber());
                            }

                            callback.onResponse(status, message, receipt);
                        }
                    });
        } catch (Exception e) {
            callback.onResponse(QUERY_FAILED, e.getMessage(), null);
        }
    }

    @Override
    public void call(Account account, CallRequest request, CallResponseCallback callback) {
        try {
            Path path = Path.decode(request.getPath());
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setName(path.getResource());
            resourceInfo.setStubType(type);

            TransactionContext context =
                    new TransactionContext(
                            toWeCrossAccount(account), path, resourceInfo, blockManager);
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setMethod(request.getMethod());
            transactionRequest.setArgs(request.getArgs());
            wecrossDriver.asyncCall(
                    context,
                    transactionRequest,
                    false,
                    luyuWeCrossConnection,
                    new com.webank.wecross.stub.Driver.Callback() {
                        @Override
                        public void onTransactionResponse(
                                TransactionException transactionException,
                                TransactionResponse transactionResponse) {
                            int status = QUERY_SUCCESS;
                            String message = "Success";
                            CallResponse callResponse = null;
                            if (transactionException != null) {
                                status = transactionException.getErrorCode();
                                message = transactionException.getMessage();
                            }

                            if (transactionResponse != null) {
                                callResponse = new CallResponse();
                                callResponse.setResult(transactionResponse.getResult());
                                callResponse.setCode(transactionResponse.getErrorCode());
                                callResponse.setMessage(transactionResponse.getMessage());
                                callResponse.setPath(request.getPath());
                                callResponse.setMethod(request.getMethod());
                                callResponse.setArgs(request.getArgs());
                            }

                            callback.onResponse(status, message, callResponse);
                        }
                    });
        } catch (Exception e) {
            callback.onResponse(QUERY_FAILED, e.getMessage(), null);
        }
    }

    @Override
    public void getTransactionReceipt(String txHash, ReceiptCallback callback) {
        // blockNumber is not used by BCOS Driver, TODO: Fabric stub support this
        wecrossDriver.asyncGetTransaction(
                txHash,
                0,
                blockManager,
                true,
                luyuWeCrossConnection,
                new com.webank.wecross.stub.Driver.GetTransactionCallback() {
                    @Override
                    public void onResponse(
                            Exception e, com.webank.wecross.stub.Transaction transaction) {
                        if (e != null) {
                            callback.onResponse(QUERY_FAILED, e.getMessage(), null);
                        } else {
                            try {
                                Path path = Path.decode(chainPath);
                                path.setResource(transaction.getResource());

                                Receipt receipt = new Receipt();
                                receipt.setResult(transaction.getTransactionResponse().getResult());
                                receipt.setCode(
                                        transaction.getTransactionResponse().getErrorCode());
                                receipt.setMessage(
                                        transaction.getTransactionResponse().getMessage());
                                receipt.setPath(path.toString());
                                receipt.setMethod(transaction.getTransactionRequest().getMethod());
                                receipt.setArgs(transaction.getTransactionRequest().getArgs());
                                receipt.setTransactionHash(txHash);
                                receipt.setTransactionBytes(transaction.getTxBytes());
                                receipt.setBlockNumber(
                                        transaction.getTransactionResponse().getBlockNumber());

                                callback.onResponse(QUERY_SUCCESS, "Success", receipt);

                            } catch (Exception e1) {
                                callback.onResponse(QUERY_FAILED, e1.getMessage(), null);
                            }
                        }
                    }
                });
    }

    @Override
    public void getBlockByHash(String blockHash, BlockCallback callback) {
        // TODO: implement this
    }

    @Override
    public void getBlockByNumber(long blockNumber, BlockCallback callback) {
        if (blockManager.hasBlock(blockNumber)) {
            blockManager.asyncGetBlock(
                    blockNumber,
                    new BlockManager.GetBlockCallback() {
                        @Override
                        public void onResponse(Exception e, Block block) {
                            if (e != null) {
                                callback.onResponse(QUERY_FAILED, e.getMessage(), null);
                            } else {
                                link.luyu.protocol.network.Block luyuBlock = toLuyuBlock(block);
                                callback.onResponse(QUERY_SUCCESS, "success", luyuBlock);
                            }
                        }
                    });
        } else {
            wecrossDriver.asyncGetBlock(
                    blockNumber,
                    false,
                    luyuWeCrossConnection,
                    new com.webank.wecross.stub.Driver.GetBlockCallback() {
                        @Override
                        public void onResponse(Exception e, Block block) {
                            if (e != null) {
                                callback.onResponse(QUERY_FAILED, e.getMessage(), null);
                            } else {
                                link.luyu.protocol.network.Block luyuBlock = toLuyuBlock(block);
                                callback.onResponse(QUERY_SUCCESS, "success", luyuBlock);
                            }
                        }
                    });
        }
    }

    private link.luyu.protocol.network.Block toLuyuBlock(Block block) {
        link.luyu.protocol.network.Block luyuBlock = new link.luyu.protocol.network.Block();
        luyuBlock.setNumber(block.blockHeader.getNumber());
        luyuBlock.setChainPath(chainPath);
        luyuBlock.setHash(block.blockHeader.getHash());
        luyuBlock.setRoots(
                new String[] {
                    block.blockHeader.getStateRoot(),
                    block.blockHeader.getTransactionRoot(),
                    block.blockHeader.getReceiptRoot()
                });
        luyuBlock.setBytes(block.rawBytes);
        luyuBlock.setParentHash(new String[] {block.blockHeader.getPrevHash()});
        luyuBlock.setTimestamp(0); // TODO: add timestamp
        luyuBlock.setTransactionHashs(block.getTransactionsHashes().toArray(new String[0]));
        return luyuBlock;
    }

    @Override
    public long getBlockNumber() {
        long blockNumber = -1;
        try {
            CompletableFuture<Long> future = new CompletableFuture<>();
            blockManager.asyncGetBlockNumber(
                    new BlockManager.GetBlockNumberCallback() {
                        @Override
                        public void onResponse(Exception e, long blockNumber) {
                            if (e != null) {
                                future.complete(new Long(-1));
                            } else {
                                future.complete(blockNumber);
                            }
                        }
                    });
            blockNumber = future.get(LuyuDefault.ADAPTER_QUERY_EXPIRES, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("getBlockNumber exception: ", e);
        }
        return blockNumber;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getSignatureType() {
        if (type.equals(BCOSConstant.BCOS_STUB_TYPE)) {
            return EcdsaSecp256k1WithSHA256.TYPE;
        } else if (type.equals(BCOSConstant.GM_BCOS_STUB_TYPE)) {
            return SM2WithSM3.TYPE;
        } else {
            logger.error("Unsupported plugin type: " + getType());
            return null;
        }
    }

    @Override
    public void listResources(ResourcesCallback callback) {
        Request request = new Request();
        request.setPath(chainPath);
        request.setType(LuyuDefault.LIST_RESOURCES);
        luyuWeCrossConnection.asyncSend(
                request,
                new Connection.Callback() {
                    @Override
                    public void onResponse(Response response) {
                        try {
                            if (response.getErrorCode() != 0) {
                                callback.onResponse(
                                        response.getErrorCode(), response.getErrorMessage(), null);
                                return;
                            } else {
                                Collection<Resource> resources = new HashSet<>();
                                resources =
                                        objectMapper.readValue(
                                                response.getData(),
                                                new TypeReference<Collection<Resource>>() {});

                                callback.onResponse(
                                        QUERY_SUCCESS,
                                        "Success",
                                        resources.toArray(new Resource[resources.size()]));
                            }

                        } catch (Exception e) {
                            callback.onResponse(QUERY_FAILED, e.getMessage(), null);
                        }
                    }
                });
    }

    private com.webank.wecross.stub.Account toWeCrossAccount(Account account) {
        return new LuyuWeCrossAccount(type, account);
    }

    @Override
    public void registerEvents(Events events) {
        logger.info("BCOS driver {} register events.", chainPath);
        this.routerEventsHandler = events;
    }

    private void handleChainSendTransactionEvent(String resourceName, byte[] data) {
        try {

            Log log = objectMapper.readValue(data, new TypeReference<Log>() {});
            if (logger.isDebugEnabled()) {
                logger.debug("handleChainSendTransactionEvent {}", log);
            }
            asyncQueryCns(
                    resourceName,
                    new AsyncCnsService.QueryCallback() {
                        @Override
                        public void onResponse(Exception e, String abi, String address) {
                            if (e != null) {
                                logger.warn("Get abi failed. name: {} e: {}", resourceName, e);
                                return;
                            }

                            LogResult logResult = null;
                            try {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "handleChainSendTransactionEvent cns back {}", abi);
                                }
                                TransactionDecoder txDecoder =
                                        TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                                logResult = txDecoder.decodeEventLogReturnObject(log);
                                Transaction transaction = toTransaction(logResult);
                                String luyuIdentity = transaction.getSender();
                                String callbackMethod =
                                        (String) logResult.getLogParams().get(5).getData();
                                String sender = (String) logResult.getLogParams().get(6).getData();

                                routerEventsHandler.getAccountByIdentity(
                                        luyuIdentity,
                                        new Events.KeyCallback() {
                                            @Override
                                            public void onResponse(Account account) {
                                                if (account == null) {
                                                    logger.error(
                                                            "Could not get account from account manager of luyuIdentity:{}",
                                                            luyuIdentity);
                                                }

                                                com.webank.wecross.stub.Account weCrossAccount =
                                                        toWeCrossAccount(account);
                                                /* TODO: enable this
                                                if (!weCrossAccount.getIdentity().equals(sender)) {
                                                    logger.warn(
                                                            "Permission denied of chain account:{} using luyu account:{} to query",
                                                            sender,
                                                            luyuIdentity);
                                                    return;
                                                }
                                                 */
                                                routerEventsHandler.sendTransaction(
                                                        transaction,
                                                        new ReceiptCallback() {
                                                            @Override
                                                            public void onResponse(
                                                                    int status,
                                                                    String message,
                                                                    Receipt receipt) {
                                                                if (logger.isDebugEnabled()) {
                                                                    logger.debug(
                                                                            "handleChainSendTransactionEvent resource response: status:{} message:{} receipt:{}",
                                                                            status,
                                                                            message,
                                                                            receipt);
                                                                }
                                                                if (status != 0
                                                                        || receipt == null) {
                                                                    logger.error(
                                                                            "Chain event sendTransaction failed. status:{}, message:{}",
                                                                            status,
                                                                            message);
                                                                    return;
                                                                }

                                                                if (receipt.getCode() != 0) {
                                                                    logger.error(
                                                                            "Chain event sendTransaction response failed. receipt:{}",
                                                                            receipt.toString());
                                                                    return;
                                                                }

                                                                sendCallbackTransaction(
                                                                        account,
                                                                        resourceName,
                                                                        callbackMethod,
                                                                        receipt.getResult(),
                                                                        transaction.getNonce());
                                                            }
                                                        });
                                            }
                                        });
                            } catch (Exception e1) {
                                logger.warn("Handle chain event failed,", e1);
                            }
                        }
                    });

        } catch (Exception e) {
            logger.warn("Parse sendTransaction event exception, ", e);
        }
    }

    private Transaction toTransaction(LogResult logResult) throws Exception {
        String path = (String) logResult.getLogParams().get(0).getData();
        String method = (String) logResult.getLogParams().get(1).getData();
        ArrayList<String> args = (ArrayList<String>) logResult.getLogParams().get(2).getData();
        BigInteger nonce = (BigInteger) logResult.getLogParams().get(3).getData();
        String luyuIdentity = (String) logResult.getLogParams().get(4).getData();

        Transaction transaction = new Transaction();
        transaction.setPath(path);
        transaction.setMethod(method);
        transaction.setArgs(args.toArray(new String[] {}));
        transaction.setNonce(nonce.longValue());
        transaction.setSender(luyuIdentity);
        return transaction;
    }

    private void handleChainCallEvent(String resourceName, byte[] data) {
        try {
            Log log = objectMapper.readValue(data, new TypeReference<Log>() {});
            if (logger.isDebugEnabled()) {
                logger.debug("handleChainCallEvent {}", log);
            }
            asyncQueryCns(
                    resourceName,
                    new AsyncCnsService.QueryCallback() {
                        @Override
                        public void onResponse(Exception e, String abi, String address) {
                            if (e != null) {
                                logger.warn("Get abi failed. name: {} e: {}", resourceName, e);
                                return;
                            }

                            LogResult logResult = null;
                            try {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("handleChainCallEvent cns back {}", abi);
                                }
                                TransactionDecoder txDecoder =
                                        TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                                logResult = txDecoder.decodeEventLogReturnObject(log);
                                CallRequest callRequest = toCallRequest(logResult);
                                String luyuIdentity = callRequest.getSender();
                                String callbackMethod =
                                        (String) logResult.getLogParams().get(5).getData();
                                String sender = (String) logResult.getLogParams().get(6).getData();

                                routerEventsHandler.getAccountByIdentity(
                                        luyuIdentity,
                                        new Events.KeyCallback() {
                                            @Override
                                            public void onResponse(Account account) {
                                                if (account == null) {
                                                    logger.error(
                                                            "Could not get account from account manager of luyuIdentity:{}",
                                                            luyuIdentity);
                                                }
                                                com.webank.wecross.stub.Account weCrossAccount =
                                                        toWeCrossAccount(account);
                                                /* TODO: enable this
                                                if (!weCrossAccount.getIdentity().equals(sender)) {
                                                    logger.warn(
                                                            "Permission denied of chain account:{} using luyu account:{} to query",
                                                            sender,
                                                            luyuIdentity);
                                                    return;
                                                }
                                                 */
                                                routerEventsHandler.call(
                                                        callRequest,
                                                        new CallResponseCallback() {
                                                            @Override
                                                            public void onResponse(
                                                                    int status,
                                                                    String message,
                                                                    CallResponse callResponse) {
                                                                if (logger.isDebugEnabled()) {
                                                                    logger.debug(
                                                                            "handleChainSendTransactionEvent resource response: status:{} message:{} receipt:{}",
                                                                            status,
                                                                            message,
                                                                            callResponse);
                                                                }

                                                                if (status != 0
                                                                        || callResponse == null) {
                                                                    logger.error(
                                                                            "Chain event call failed. status:{}, message:{}",
                                                                            status,
                                                                            message);
                                                                    return;
                                                                }

                                                                if (callResponse.getCode() != 0) {
                                                                    logger.error(
                                                                            "Chain event call response failed. receipt:{}",
                                                                            callResponse
                                                                                    .toString());
                                                                    return;
                                                                }

                                                                sendCallbackTransaction(
                                                                        account,
                                                                        resourceName,
                                                                        callbackMethod,
                                                                        callResponse.getResult(),
                                                                        callRequest.getNonce());
                                                            }
                                                        });
                                            }
                                        });
                            } catch (Exception e1) {
                                logger.warn("Handle chain event failed,", e1);
                            }
                        }
                    });
        } catch (Exception e) {
            logger.warn("Parse call event exception, ", e);
        }
    }

    private CallRequest toCallRequest(LogResult logResult) throws Exception {
        String path = (String) logResult.getLogParams().get(0).getData();
        String method = (String) logResult.getLogParams().get(1).getData();
        ArrayList<String> args = (ArrayList<String>) logResult.getLogParams().get(2).getData();
        BigInteger nonce = (BigInteger) logResult.getLogParams().get(3).getData();
        String luyuIdentity = (String) logResult.getLogParams().get(4).getData();

        CallRequest callRequest = new CallRequest();
        callRequest.setPath(path);
        callRequest.setMethod(method);
        callRequest.setArgs(args.toArray(new String[] {}));
        callRequest.setNonce(nonce.longValue());
        callRequest.setSender(luyuIdentity);
        return callRequest;
    }

    private void sendCallbackTransaction(
            Account account,
            String resourceName,
            String callbackMethod,
            String[] args,
            long nonce) {
        Path callbackPath = null;
        try {
            callbackPath = Path.decode(chainPath);
            callbackPath.setResource(resourceName);
        } catch (Exception e) {
            logger.error("Chain path decode error, e:", e);
        }

        ArrayList<String> callbackArgs = new ArrayList<>();
        callbackArgs.add(new Long(nonce).toString()); // set
        // nonce
        for (String arg : args) {
            callbackArgs.add(arg);
        }
        Transaction callbackTx = new Transaction();
        callbackTx.setPath(callbackPath.toString());
        callbackTx.setMethod(callbackMethod);
        callbackTx.setArgs(callbackArgs.toArray(new String[] {}));
        // callbackTx.setSender(luyuIdentity);
        callbackTx.setNonce(nonce);

        sendTransaction(
                account,
                callbackTx,
                new ReceiptCallback() {
                    @Override
                    public void onResponse(int status, String message, Receipt receipt) {
                        logger.debug("CallbackTx sended. {} {} {}", status, message, receipt);
                    }
                });
    }

    private void asyncQueryCns(String resourceName, AsyncCnsService.QueryCallback callback) {
        wecrossDriver
                .getAsyncCnsService()
                .queryABI(resourceName, wecrossDriver, luyuWeCrossConnection, callback);
    }
}
