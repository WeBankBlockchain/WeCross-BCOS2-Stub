package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The implementation of connection for BCOS */
public class BCOSConnection implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(BCOSConnection.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private List<ResourceInfo> resourceInfoList;

    private List<ResourceInfo> resourcesCache = null;

    private ConnectionEventHandler eventHandler = null;

    private final Web3jWrapper web3jWrapper;

    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(32);

    private Map<String, String> properties = new HashMap<>();

    public BCOSConnection(Web3jWrapper web3jWrapper) {
        this.web3jWrapper = web3jWrapper;
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        this.scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    if (Objects.nonNull(eventHandler)) {
                        noteOnResourcesChange();
                    }
                },
                10000,
                30000,
                TimeUnit.MILLISECONDS);
    }

    private void noteOnResourcesChange() {
        synchronized (this) {
            List<ResourceInfo> resources = getResources();
            if (!resources.equals(resourcesCache) && !resources.isEmpty()) {
                eventHandler.onResourcesChange(resources);
                resourcesCache = resources;
                if (logger.isDebugEnabled()) {
                    logger.debug(" resources notify, resources: {}", resources);
                }
            }
        }
    }

    public List<ResourceInfo> getResourceInfoList() {
        return resourceInfoList;
    }

    public void setResourceInfoList(List<ResourceInfo> resourceInfoList) {
        this.resourceInfoList = resourceInfoList;
    }

    public Web3jWrapper getWeb3jWrapper() {
        return web3jWrapper;
    }

    public List<ResourceInfo> getResourcesCache() {
        return resourcesCache;
    }

    public void setResourcesCache(List<ResourceInfo> resourcesCache) {
        this.resourcesCache = resourcesCache;
    }

    public List<ResourceInfo> getResources() {
        List<ResourceInfo> resources =
                new ArrayList<ResourceInfo>() {
                    {
                        addAll(resourceInfoList);
                    }
                };

        String[] paths = listPaths();
        if (Objects.nonNull(paths)) {
            for (String path : paths) {
                ResourceInfo resourceInfo = new ResourceInfo();
                resourceInfo.setStubType(properties.get(BCOSConstant.BCOS_STUB_TYPE));
                resourceInfo.setName(path.split("\\.")[2]);
                Map<Object, Object> resourceProperties = new HashMap<>();
                resourceProperties.put(
                        BCOSConstant.BCOS_GROUP_ID, properties.get(BCOSConstant.BCOS_GROUP_ID));
                resourceProperties.put(
                        BCOSConstant.BCOS_CHAIN_ID, properties.get(BCOSConstant.BCOS_CHAIN_ID));
                resourceInfo.setProperties(resourceProperties);
                resources.add(resourceInfo);
            }
        }
        return resources;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void setProperty(Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    /** list paths stored in proxy contract */
    public String[] listPaths() {
        Function function =
                FunctionUtility.newDefaultFunction(
                        BCOSConstant.PROXY_METHOD_GETPATHS, new String[] {});
        String address = properties.get(BCOSConstant.BCOS_PROXY_NAME);
        try {
            Call.CallOutput callOutput =
                    web3jWrapper.call(
                            BCOSConstant.DEFAULT_ADDRESS,
                            address,
                            FunctionEncoder.encode(function));

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " listPaths, status: {}, blk: {}, output: {}",
                        callOutput.getStatus(),
                        callOutput.getCurrentBlockNumber(),
                        callOutput.getOutput());
            }

            if (StatusCode.Success.equals(callOutput.getStatus())) {
                String[] paths = FunctionUtility.decodeDefaultOutput(callOutput.getOutput());
                if (Objects.nonNull(paths) && paths.length != 0) {
                    Set<String> set = new HashSet<>(Arrays.asList(paths));
                    set.add("a.b." + BCOSConstant.BCOS_PROXY_NAME);
                    return set.toArray(new String[0]);
                } else {
                    Set<String> set = new HashSet<>();
                    set.add("a.b." + BCOSConstant.BCOS_PROXY_NAME);
                    logger.debug(" listPaths empty and add proxy ");
                    return set.toArray(new String[0]);
                }
            } else {
                logger.warn(" listPaths failed, status {}", callOutput.getStatus());
                return null;
            }
        } catch (Exception e) {
            logger.warn(" listPaths failed,", e);
            return null;
        }
    }

    /**
     * async operations
     *
     * @param request
     * @param callback
     */
    @Override
    public void asyncSend(Request request, Callback callback) {
        if (request.getType() == BCOSRequestType.SEND_TRANSACTION) {
            handleAsyncTransactionRequest(request, callback);
        } else if (request.getType() == BCOSRequestType.GET_BLOCK_BY_NUMBER) {
            handleAsyncGetBlockRequest(request, callback);
        } else if (request.getType() == BCOSRequestType.GET_BLOCK_NUMBER) {
            handleAsyncGetBlockNumberRequest(callback);
        } else if (request.getType() == BCOSRequestType.GET_TRANSACTION_PROOF) {
            asyncGetTransactionProof(request, callback);
        } else if (request.getType() == BCOSRequestType.CALL) {
            handleAsyncCallRequest(request, callback);
        } else {
            // Does not support asynchronous operation, async to sync
            logger.warn(" unrecognized request type, type: {}", request.getType());
            Response response = new Response();
            response.setErrorCode(BCOSStatusCode.UnrecognizedRequestType);
            response.setErrorMessage(
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.UnrecognizedRequestType)
                            + " ,type: "
                            + request.getType());
            callback.onResponse(response);
        }
    }

    public void handleAsyncCallRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            TransactionParams transaction =
                    objectMapper.readValue(request.getData(), TransactionParams.class);

            Call.CallOutput callOutput =
                    web3jWrapper.call(
                            transaction.getFrom(), transaction.getTo(), transaction.getData());

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " accountAddress: {}, contractAddress: {}, data: {}, status: {}, current blk: {}, output: {}",
                        transaction.getFrom(),
                        transaction.getTo(),
                        transaction.getData(),
                        callOutput.getStatus(),
                        callOutput.getCurrentBlockNumber(),
                        callOutput.getOutput());
            }

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
            response.setData(objectMapper.writeValueAsBytes(callOutput));
        } catch (Exception e) {
            logger.warn(" handleCallRequest Exception, e:", e);
            response.setErrorCode(BCOSStatusCode.HandleCallRequestFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }

        callback.onResponse(response);
    }

    public void handleAsyncTransactionRequest(Request request, Callback callback) {

        Response response = new Response();
        try {
            TransactionParams transaction =
                    objectMapper.readValue(request.getData(), TransactionParams.class);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " from: {}, to: {}, tx: {} ",
                        transaction.getFrom(),
                        transaction.getTo(),
                        transaction.getData());
            }

            web3jWrapper.sendTransactionAndGetProof(
                    transaction.getData(),
                    new TransactionSucCallback() {
                        @Override
                        public void onResponse(TransactionReceipt receipt) {
                            if (Objects.isNull(receipt)
                                    || Objects.isNull(receipt.getTransactionHash())
                                    || "".equals(receipt.getTransactionHash())
                                    || (new BigInteger(
                                                            receipt.getTransactionHash()
                                                                    .substring(2),
                                                            16)
                                                    .compareTo(BigInteger.ZERO)
                                            == 0)) {
                                response.setErrorCode(BCOSStatusCode.TransactionReceiptNotExist);
                                response.setErrorMessage(
                                        BCOSStatusCode.getStatusMessage(
                                                BCOSStatusCode.TransactionReceiptNotExist));
                            } else {
                                try {
                                    response.setErrorCode(BCOSStatusCode.Success);
                                    response.setErrorMessage(
                                            BCOSStatusCode.getStatusMessage(
                                                    BCOSStatusCode.Success));
                                    response.setData(objectMapper.writeValueAsBytes(receipt));
                                } catch (JsonProcessingException e) {
                                    logger.error(" e:", e);
                                    response.setErrorCode(
                                            BCOSStatusCode.HandleSendTransactionFailed);
                                    response.setErrorMessage(" errorMessage: " + e.getMessage());
                                }
                            }

                            callback.onResponse(response);

                            // trigger resources sync after cns updated
                            if (transaction.getTransactionRequest() != null
                                    && transaction
                                            .getTransactionRequest()
                                            .getMethod()
                                            .equals(BCOSConstant.PROXY_METHOD_ADDPATH)) {

                                scheduledExecutorService.schedule(
                                        () -> noteOnResourcesChange(), 1, TimeUnit.MILLISECONDS);
                            }
                        }
                    });
        } catch (Exception e) {
            logger.error(" e: ", e);
            response.setErrorCode(BCOSStatusCode.HandleSendTransactionFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
            callback.onResponse(response);
        }
    }

    public void handleAsyncGetBlockNumberRequest(Callback callback) {
        Response response = new Response();
        try {
            BigInteger blockNumber = web3jWrapper.getBlockNumber();

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));

            response.setData(blockNumber.toByteArray());
            logger.debug(" blockNumber: {}", blockNumber);
        } catch (Exception e) {
            logger.warn(" handleGetBlockNumberRequest Exception, e: ", e);
            response.setErrorCode(BCOSStatusCode.HandleGetBlockNumberFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        callback.onResponse(response);
    }

    /**
     * get TransAndProof and ReceiptAndProof by transaction hash
     *
     * @param request
     */
    private void asyncGetTransactionProof(Request request, Callback callback) {
        String txHash = new String(request.getData(), StandardCharsets.UTF_8);
        Response response = new Response();
        try {
            // get transaction and transaction merkle proof
            TransactionWithProof.TransAndProof transAndProof =
                    web3jWrapper.getTransactionByHashWithProof(txHash);

            if (Objects.isNull(transAndProof)
                    || Objects.isNull(transAndProof.getTransaction())
                    || Objects.isNull(transAndProof.getTransaction().getHash())
                    || transAndProof
                            .getTransaction()
                            .getHash()
                            .equals(
                                    "0x0000000000000000000000000000000000000000000000000000000000000000")) {
                response.setErrorCode(BCOSStatusCode.TransactionReceiptProofNotExist);
                response.setErrorMessage(" Not found transaction proof, tx hash: " + txHash);
                callback.onResponse(response);
                return;
            }

            TransactionReceiptWithProof.ReceiptAndProof receiptAndProof =
                    web3jWrapper.getTransactionReceiptByHashWithProof(txHash);
            if (Objects.isNull(receiptAndProof)
                    || Objects.isNull(receiptAndProof.getTransactionReceipt())
                    || Objects.isNull(
                            receiptAndProof.getTransactionReceipt().getTransactionHash())) {
                response.setErrorCode(BCOSStatusCode.TransactionReceiptProofNotExist);
                response.setErrorMessage(" Not found transaction proof, tx hash: " + txHash);
                callback.onResponse(response);
                return;
            }

            TransactionProof transactionProof =
                    new TransactionProof(transAndProof, receiptAndProof);

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
            response.setData(objectMapper.writeValueAsBytes(transactionProof));
            logger.debug(
                    " getTransactionProof, tx hash: {}, transAndProof: {}, receiptAndProof: {}",
                    txHash,
                    transactionProof.getTransAndProof(),
                    transactionProof.getReceiptAndProof());
            callback.onResponse(response);
        } catch (Exception e) {
            response.setErrorMessage(e.getMessage());
            response.setErrorCode(BCOSStatusCode.UnclassifiedError);
            callback.onResponse(response);
        }
    }

    public void handleAsyncGetBlockRequest(Request request, Callback callback) {
        Response response = new Response();
        try {
            BigInteger blockNumber = new BigInteger(request.getData());
            BcosBlock.Block block = web3jWrapper.getBlockByNumber(blockNumber.longValue());

            BcosBlockHeader.BlockHeader blockHeader =
                    web3jWrapper.getBlockHeaderByNumber(blockNumber.longValue());

            List<String> headerData = new ArrayList<>();
            headerData.add(objectMapper.writeValueAsString(blockHeader));
            block.setExtraData(headerData);
            logger.debug("handleAsyncGetBlockRequest: block.Ext: {}", headerData);
            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
            response.setData(objectMapper.writeValueAsBytes(block));
            if (logger.isDebugEnabled()) {
                logger.debug(" getBlockByNumber, blockNumber: {}, block: {}", blockNumber, block);
            }
        } catch (Exception e) {
            logger.warn(" Exception, e: ", e);
            response.setErrorCode(BCOSStatusCode.HandleGetBlockFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        callback.onResponse(response);
    }

    public boolean hasProxyDeployed() {
        return getProperties().containsKey(BCOSConstant.BCOS_PROXY_NAME);
    }
}
