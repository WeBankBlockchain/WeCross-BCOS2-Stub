package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The implementation of connection for BCOS */
public class BCOSConnection implements Connection {

    private static final Logger logger = LoggerFactory.getLogger(BCOSConnection.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private List<ResourceInfo> resourceInfoList;

    private final Web3jWrapper web3jWrapper;

    public BCOSConnection(Web3jWrapper web3jWrapper) {
        this.web3jWrapper = web3jWrapper;
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
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

    @Override
    public List<ResourceInfo> getResources() {
        return resourceInfoList;
    }

    /**
     * sync operations
     *
     * @param request
     * @return
     */
    @Override
    public Response send(Request request) {
        switch (request.getType()) {
            case BCOSRequestType.CALL:
                return handleCallRequest(request);
            case BCOSRequestType.SEND_TRANSACTION:
                return handleTransactionRequest(request);
            case BCOSRequestType.GET_BLOCK_HEADER:
                return handleGetBlockHeaderRequest(request);
            case BCOSRequestType.GET_BLOCK_NUMBER:
                return handleGetBlockNumberRequest(request);
            case BCOSRequestType.GET_TRANSACTION_PROOF:
                return handleGetTransactionProof(request);
            default:
                logger.warn(" unrecognized request type, type: {}", request.getType());
                Response response = new Response();
                response.setErrorCode(BCOSStatusCode.UnrecognizedRequestType);
                response.setErrorMessage(
                        BCOSStatusCode.getStatusMessage(BCOSStatusCode.UnrecognizedRequestType)
                                + " ,type: "
                                + request.getType());
                return response;
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
        } else {
            // Does not support asynchronous operation, async to sync
            callback.onResponse(send(request));
        }
    }

    public Response handleCallRequest(Request request) {
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
            logger.warn(" handleCallRequest Exception, e: {}", e);
            response.setErrorCode(BCOSStatusCode.HandleCallRequestFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        return response;
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
                                    logger.error(" e: {} ", e);
                                    response.setErrorCode(
                                            BCOSStatusCode.HandleSendTransactionFailed);
                                    response.setErrorMessage(" errorMessage: " + e.getMessage());
                                }
                            }

                            callback.onResponse(response);
                        }
                    });
        } catch (Exception e) {
            logger.error(" e: {} ", e);
            response.setErrorCode(BCOSStatusCode.HandleSendTransactionFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
            callback.onResponse(response);
        }
    }

    public Response handleTransactionRequest(Request request) {

        class SendTransactionResponseCallback implements Callback {
            private Response response;
            private Semaphore semaphore = new Semaphore(1, true);

            public SendTransactionResponseCallback() {
                try {
                    this.semaphore.acquire();
                } catch (InterruptedException e) {
                    logger.error(" e: {}", e);
                    Thread.currentThread().interrupt();
                }
            }

            public Response getResponse() {
                return response;
            }

            public Semaphore getSemaphore() {
                return semaphore;
            }

            @Override
            public void onResponse(Response response) {
                this.response = response;
                this.getSemaphore().release();
            }
        }

        SendTransactionResponseCallback sendTxResponse = new SendTransactionResponseCallback();
        handleAsyncTransactionRequest(request, sendTxResponse);
        try {
            sendTxResponse.getSemaphore().acquire(1);
        } catch (InterruptedException e) {
            logger.error(" e: {}", e);
            Thread.currentThread().interrupt();
        }
        return sendTxResponse.getResponse();
    }

    public Response handleGetBlockNumberRequest(Request request) {
        Response response = new Response();
        try {
            BigInteger blockNumber = web3jWrapper.getBlockNumber();

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));

            response.setData(blockNumber.toByteArray());
            logger.debug(" blockNumber: {}", blockNumber);
        } catch (Exception e) {
            logger.warn(" handleGetBlockNumberRequest Exception, e: {}", e);
            response.setErrorCode(BCOSStatusCode.HandleGetBlockNumberFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        return response;
    }

    /**
     * get TransAndProof and ReceiptAndProof by transaction hash
     *
     * @param txHash
     * @return
     * @throws IOException
     */
    public TransactionProof getTransactionProof(String txHash)
            throws IOException, BCOSStubException {
        // get transaction and transaction merkle proof
        TransactionWithProof.TransAndProof transAndProof =
                web3jWrapper.getTransactionByHashWithProof(txHash);

        if (Objects.isNull(transAndProof)
                || Objects.isNull(transAndProof.getTransaction())
                || Objects.isNull(transAndProof.getTransaction().getHash())) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionProofNotExist,
                    " Not found transaction proof, tx hash: " + txHash);
        }

        TransactionReceiptWithProof.ReceiptAndProof receiptAndProof =
                web3jWrapper.getTransactionReceiptByHashWithProof(txHash);
        if (Objects.isNull(receiptAndProof)
                || Objects.isNull(receiptAndProof.getTransactionReceipt())
                || Objects.isNull(receiptAndProof.getTransactionReceipt().getTransactionHash())) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionReceiptProofNotExist,
                    " Not found transaction receipt proof, tx hash: " + txHash);
        }

        return new TransactionProof(transAndProof, receiptAndProof);
    }

    public Response handleGetTransactionProof(Request request) {
        Response response = new Response();
        try {
            String txHash = new String(request.getData(), StandardCharsets.UTF_8);
            TransactionProof transactionProof = getTransactionProof(txHash);

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));

            response.setData(objectMapper.writeValueAsBytes(transactionProof));
            logger.debug(
                    " getTransactionProof, tx hash: {}, transAndProof: {}, receiptAndProof: {}",
                    txHash,
                    transactionProof.getTransAndProof(),
                    transactionProof.getReceiptAndProof());
        } catch (BCOSStubException e) {
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn(" handleGetTransactionProof Exception, e: {}", e);
            response.setErrorCode(BCOSStatusCode.HandleGetTransactionProofFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }

        return response;
    }

    /**
     * convert Block to BlockHeader
     *
     * @param block
     * @return
     */
    public BlockHeader convertToBlockHeader(BcosBlock.Block block) {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHash(block.getHash());
        blockHeader.setPrevHash(block.getParentHash());
        blockHeader.setNumber(block.getNumber().longValue());
        blockHeader.setReceiptRoot(block.getReceiptsRoot());
        blockHeader.setStateRoot(block.getStateRoot());
        blockHeader.setTransactionRoot(block.getTransactionsRoot());
        return blockHeader;
    }

    public Response handleGetBlockHeaderRequest(Request request) {
        Response response = new Response();
        try {
            BigInteger blockNumber = new BigInteger(request.getData());
            BcosBlock.Block block = web3jWrapper.getBlockByNumber(blockNumber.longValue());

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
            response.setData(objectMapper.writeValueAsBytes(convertToBlockHeader(block)));
            logger.debug(" getBlockByNumber, blockNumber: {}, block: {}", blockNumber, block);
        } catch (Exception e) {
            logger.warn(" Exception, e: {}", e);
            response.setErrorCode(BCOSStatusCode.HandleGetBlockHeaderFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        return response;
    }
}
