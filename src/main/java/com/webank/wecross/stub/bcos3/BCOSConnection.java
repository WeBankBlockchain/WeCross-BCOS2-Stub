package com.webank.wecross.stub.bcos3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.bcos3.client.AbstractClientWrapper;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import com.webank.wecross.stub.bcos3.common.BCOSRequestType;
import com.webank.wecross.stub.bcos3.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos3.common.ObjectMapperFactory;
import com.webank.wecross.stub.bcos3.contract.FunctionUtility;
import com.webank.wecross.stub.bcos3.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos3.protocol.response.TransactionPair;
import com.webank.wecross.stub.bcos3.protocol.response.TransactionProof;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.sdk.v3.client.exceptions.ClientException;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.v3.client.protocol.response.Call;
import org.fisco.bcos.sdk.v3.codec.FunctionEncoderInterface;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The implementation of connection for BCOS */
public class BCOSConnection implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(BCOSConnection.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private List<ResourceInfo> resourceInfoList;

    private List<ResourceInfo> resourcesCache = null;

    private ConnectionEventHandler eventHandler = null;

    private final AbstractClientWrapper clientWrapper;

    private final ScheduledExecutorService scheduledExecutorService;

    private Map<String, String> properties = new HashMap<>();

    private final FunctionEncoderInterface functionEncoder;

    public BCOSConnection(
            AbstractClientWrapper clientWrapper,
            ScheduledExecutorService scheduledExecutorService) {
        this.clientWrapper = clientWrapper;
        this.functionEncoder =
                (clientWrapper.getClient().isWASM()
                        ? new org.fisco.bcos.sdk.v3.codec.scale.FunctionEncoder(
                                clientWrapper.getCryptoSuite())
                        : new org.fisco.bcos.sdk.v3.codec.abi.FunctionEncoder(
                                clientWrapper.getCryptoSuite()));
        this.scheduledExecutorService = scheduledExecutorService;
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

    public AbstractClientWrapper getClientWrapper() {
        return clientWrapper;
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
                FunctionUtility.newDefaultFunction(BCOSConstant.PROXY_METHOD_GETPATHS, null);
        String address = properties.get(BCOSConstant.BCOS_PROXY_NAME);
        try {
            Call.CallOutput callOutput =
                    clientWrapper.call(
                            BCOSConstant.DEFAULT_ADDRESS,
                            address,
                            functionEncoder.encode(function));

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " listPaths, status: {}, blk: {}, output: {}",
                        callOutput.getStatus(),
                        callOutput.getBlockNumber(),
                        callOutput.getOutput());
            }

            if (Objects.equals(
                    TransactionReceiptStatus.Success.getCode(), callOutput.getStatus())) {
                String[] paths = FunctionUtility.decodeDefaultOutput(callOutput.getOutput());
                Set<String> set = new LinkedHashSet<>();
                if (Objects.nonNull(paths) && paths.length != 0) {
                    for (int i = paths.length - 1; i >= 0; i--) {
                        set.add(paths[i]);
                    }
                    set.add("a.b." + BCOSConstant.BCOS_PROXY_NAME);
                    set.add("a.b." + BCOSConstant.BCOS_HUB_NAME);
                } else {
                    set.add("a.b." + BCOSConstant.BCOS_PROXY_NAME);
                    set.add("a.b." + BCOSConstant.BCOS_HUB_NAME);
                    logger.debug("No path found and add system resources");
                }
                return set.toArray(new String[0]);
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
        } else if (request.getType() == BCOSRequestType.GET_TRANSACTION) {
            asyncGetTransaction(request, callback);
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
                    clientWrapper.call(
                            transaction.getFrom(),
                            transaction.getTo(),
                            Hex.decode(transaction.getData()));

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " accountAddress: {}, contractAddress: {}, data: {}, status: {}, current blk: {}, output: {}",
                        transaction.getFrom(),
                        transaction.getTo(),
                        transaction.getData(),
                        callOutput.getStatus(),
                        callOutput.getBlockNumber(),
                        callOutput.getOutput());
            }

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
            response.setData(objectMapper.writeValueAsBytes(callOutput));
        } catch (Exception e) {
            logger.warn("handleCallRequest Exception:", e);
            response.setErrorCode(BCOSStatusCode.HandleCallRequestFailed);
            response.setErrorMessage(e.getMessage());
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

            clientWrapper.sendTransaction(
                    transaction.getData(),
                    new TransactionCallback() {
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
                                    response.setErrorMessage(e.getMessage());
                                }
                            }

                            callback.onResponse(response);

                            // trigger resources sync after cns updated
                            if (transaction.getTransactionRequest() != null
                                    && (transaction
                                                    .getTransactionRequest()
                                                    .getMethod()
                                                    .equals(BCOSConstant.PROXY_METHOD_DEPLOY)
                                            || transaction
                                                    .getTransactionRequest()
                                                    .getMethod()
                                                    .equals(BCOSConstant.PROXY_METHOD_REGISTER))) {

                                scheduledExecutorService.schedule(
                                        () -> noteOnResourcesChange(), 1, TimeUnit.MILLISECONDS);
                            }
                        }
                    });
        } catch (Exception e) {
            logger.error("handleAsyncTransaction exception:", e);
            response.setErrorCode(BCOSStatusCode.HandleSendTransactionFailed);
            response.setErrorMessage(e.getMessage());
            callback.onResponse(response);
        }
    }

    public void handleAsyncGetBlockNumberRequest(Callback callback) {
        Response response = new Response();
        try {
            BigInteger blockNumber = clientWrapper.getBlockNumber();

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));

            response.setData(blockNumber.toByteArray());
            logger.debug(" blockNumber: {}", blockNumber);
        } catch (Exception e) {
            logger.warn(" handleGetBlockNumberRequest Exception, e: ", e);
            response.setErrorCode(BCOSStatusCode.HandleGetBlockNumberFailed);
            response.setErrorMessage(e.getMessage());
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
            JsonTransactionResponse transAndProof =
                    clientWrapper.getTransactionByHashWithProof(txHash);

            if (Objects.isNull(transAndProof) || Objects.isNull(transAndProof.getHash())) {
                response.setErrorCode(BCOSStatusCode.TransactionReceiptProofNotExist);
                response.setErrorMessage("Transaction proof not found, tx hash: " + txHash);
                callback.onResponse(response);
                return;
            }

            TransactionReceipt receiptAndProof =
                    clientWrapper.getTransactionReceiptByHashWithProof(txHash);
            if (Objects.isNull(receiptAndProof)
                    || Objects.isNull(receiptAndProof.getTransactionHash())) {
                response.setErrorCode(BCOSStatusCode.TransactionReceiptProofNotExist);
                response.setErrorMessage("Transaction proof not found, tx hash: " + txHash);
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
                    transactionProof.getTransWithProof(),
                    transactionProof.getReceiptWithProof());
            callback.onResponse(response);
        } catch (UnsupportedOperationException e) {
            response.setErrorCode(BCOSStatusCode.UnsupportedRPC);
            response.setErrorMessage(e.getMessage());
            callback.onResponse(response);
        } catch (ClientException e) {
            response.setErrorCode(BCOSStatusCode.TransactionReceiptProofNotExist);
            response.setErrorMessage("transaction proof not found, tx hash: " + txHash);
            callback.onResponse(response);
        } catch (Exception e) {
            response.setErrorCode(BCOSStatusCode.UnclassifiedError);
            response.setErrorMessage(e.getMessage());
            callback.onResponse(response);
        }
    }

    /**
     * get transaction
     *
     * @param request
     */
    private void asyncGetTransaction(Request request, Callback callback) {
        String txHash = new String(request.getData(), StandardCharsets.UTF_8);
        Response response = new Response();
        try {
            JsonTransactionResponse transaction = clientWrapper.getTransaction(txHash);
            TransactionReceipt transactionReceipt = clientWrapper.getTransactionReceipt(txHash);

            if (Objects.isNull(transaction)
                    || Objects.isNull(transaction.getHash())
                    || Objects.isNull(transactionReceipt)
                    || Objects.isNull(transactionReceipt.getTransactionHash())) {
                response.setErrorCode(BCOSStatusCode.TransactionNotExist);
                response.setErrorMessage("transaction not found, tx hash: " + txHash);
                callback.onResponse(response);
                return;
            }

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
            response.setData(
                    objectMapper.writeValueAsBytes(
                            new TransactionPair(transaction, transactionReceipt)));

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " getTransaction, tx hash: {}, transaction: {}, transactionReceipt: {}",
                        txHash,
                        transaction,
                        transactionReceipt);
            }

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
            BcosBlock.Block block = clientWrapper.getBlockByNumber(blockNumber.longValue());

            try {
                BcosBlockHeader.BlockHeader blockHeader =
                        clientWrapper.getBlockHeaderByNumber(blockNumber.longValue());
                String headerData = objectMapper.writeValueAsString(blockHeader);
                block.setExtraData(headerData);

                if (logger.isDebugEnabled()) {
                    logger.debug("handleAsyncGetBlockRequest: block.Ext: {}", headerData);
                }
            } catch (UnsupportedOperationException e) {
                logger.debug(" UnsupportedOperationException getBlockHeaderByNumber");
            }

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
            response.setData(objectMapper.writeValueAsBytes(block));
            if (logger.isDebugEnabled()) {
                logger.debug(" getBlockByNumber, blockNumber: {}, block: {}", blockNumber, block);
            }
        } catch (Exception e) {
            logger.warn(" Exception, e: ", e);
            response.setErrorCode(BCOSStatusCode.HandleGetBlockFailed);
            response.setErrorMessage(e.getMessage());
        }
        callback.onResponse(response);
    }

    public boolean hasProxyDeployed() {
        return getProperties().containsKey(BCOSConstant.BCOS_PROXY_NAME);
    }

    public boolean hasHubDeployed() {
        return getProperties().containsKey(BCOSConstant.BCOS_HUB_NAME);
    }

    public String getHubAddress() {
        return getProperties().get(BCOSConstant.BCOS_HUB_NAME);
    }
}
