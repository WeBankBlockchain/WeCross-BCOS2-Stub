package com.webank.wecross.stub.bcos;

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
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
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

    public Response handleCallRequest(Request request) {
        Response response = new Response();
        try {
            String params = new String(request.getData(), StandardCharsets.UTF_8);
            String[] split = params.split(",");

            Call.CallOutput callOutput = web3jWrapper.call(split[0], split[1]);

            logger.debug(
                    " contractAddress: {}, data: {}, status: {}, current blk: {}, output: {}",
                    split[0],
                    split[1],
                    callOutput.getStatus(),
                    callOutput.getCurrentBlockNumber(),
                    callOutput.getOutput());

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

    public Response handleTransactionRequest(Request request) {
        Response response = new Response();
        try {
            String signTx = new String(request.getData(), StandardCharsets.UTF_8);

            logger.debug(" signTx: {}", signTx);

            TransactionReceipt receipt = web3jWrapper.sendTransaction(signTx);
            if (Objects.isNull(receipt)
                    || Objects.isNull(receipt.getTransactionHash())
                    || "".equals(receipt.getTransactionHash())) {
                throw new BCOSStubException(
                        BCOSStatusCode.TransactionReceiptNotExist,
                        BCOSStatusCode.getStatusMessage(BCOSStatusCode.TransactionReceiptNotExist));
            }

            TransactionProof transactionProof = getTransactionProof(receipt.getTransactionHash());

            response.setErrorCode(BCOSStatusCode.Success);
            response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
            response.setData(objectMapper.writeValueAsBytes(transactionProof));
            logger.debug(" sendTransaction, transaction proof: {}", transactionProof);

        } catch (BCOSStubException e) {
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn(
                    " handleCallRequest Exception, request type: {}, e: {}", request.getType(), e);
            response.setErrorCode(BCOSStatusCode.HandleSendTransactionFailed);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }
        return response;
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
