package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionDecoder;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.MerkleProofUtility;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Driver implementation for BCOS */
public class BCOSDriver implements Driver {

    private static final Logger logger = LoggerFactory.getLogger(BCOSDriver.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public BCOSDriver() {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * create Request object
     *
     * @param type
     * @param content
     * @return Request
     */
    private Request requestBuilder(int type, String content) {
        return requestBuilder(type, content.getBytes(StandardCharsets.UTF_8));
    }

    private Request requestBuilder(int type, byte[] content) {
        Request request = new Request();
        request.setType(type);
        request.setData(content);
        return request;
    }

    @Override
    public TransactionContext<TransactionRequest> decodeTransactionRequest(byte[] data) {
        try {
            TransactionParams transactionParams =
                    objectMapper.readValue(data, TransactionParams.class);

            if (logger.isDebugEnabled()) {
                logger.debug(" TransactionParams: {}", transactionParams);
            }

            Objects.requireNonNull(
                    transactionParams.getTransactionRequest(), "TransactionRequest is null");
            Objects.requireNonNull(transactionParams.getData(), "Data is null");

            TransactionRequest transactionRequest = transactionParams.getTransactionRequest();
            // Validating abi
            String abi = transactionParams.getData();
            if (Objects.isNull(transactionParams.getTo())) {
                // SendTransaction Operation
                ExtendedRawTransaction extendedRawTransaction =
                        ExtendedTransactionDecoder.decode(transactionParams.getData());
                abi = "0x" + extendedRawTransaction.getData();
            }

            Function function =
                    FunctionUtility.newFunction(
                            transactionRequest.getMethod(), transactionRequest.getArgs());

            String encodeAbi = FunctionEncoder.encode(function);
            if (!encodeAbi.equals(abi)) {
                logger.error(
                        " Validating abi failed, method: {}, args: {}, abi: {}, encodeAbi: {} ",
                        transactionRequest.getMethod(),
                        transactionRequest.getArgs(),
                        abi,
                        encodeAbi);
                throw new IllegalArgumentException(" Validating abi failed ");
            }

            return new TransactionContext<TransactionRequest>(transactionRequest, null, null, null);

        } catch (Exception e) {
            logger.error(" decodeTransactionRequest Exception: {}", e);
            return null;
        }
    }

    @Override
    public boolean isTransaction(Request request) {
        return (request.getType() == BCOSRequestType.SEND_TRANSACTION)
                || (request.getType() == BCOSRequestType.CALL);
    }

    @Override
    public BlockHeader decodeBlockHeader(byte[] data) {
        try {
            return objectMapper.readValue(data, BlockHeader.class);
        } catch (Exception e) {
            logger.error(" decodeBlockHeader Exception: {}", e);
            return null;
        }
    }

    @Override
    public TransactionResponse call(
            TransactionContext<TransactionRequest> request, Connection connection)
            throws TransactionException {

        TransactionResponse response = new TransactionResponse();

        try {
            ResourceInfo resourceInfo = request.getResourceInfo();
            Map<Object, Object> properties = resourceInfo.getProperties();

            // input validation
            checkRequest(request);
            checkProperties(resourceInfo.getName(), properties);

            String contractAddress = (String) properties.get(resourceInfo.getName());
            // Function object
            Function function =
                    FunctionUtility.newFunction(
                            request.getData().getMethod(), request.getData().getArgs());

            // BCOSAccount to get credentials to sign the transaction
            BCOSAccount bcosAccount = (BCOSAccount) request.getAccount();
            Credentials credentials = bcosAccount.getCredentials();

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " name:{}, address: {}, method: {}, args: {}",
                        resourceInfo.getName(),
                        contractAddress,
                        request.getData().getMethod(),
                        request.getData().getArgs());
            }

            TransactionParams transaction =
                    new TransactionParams(
                            request.getData(),
                            FunctionEncoder.encode(function),
                            credentials.getAddress(),
                            contractAddress);
            Request req =
                    requestBuilder(
                            BCOSRequestType.CALL, objectMapper.writeValueAsBytes(transaction));
            Response resp = connection.send(req);
            if (resp.getErrorCode() != BCOSStatusCode.Success) {
                throw new BCOSStubException(resp.getErrorCode(), resp.getErrorMessage());
            }

            Call.CallOutput callOutput =
                    objectMapper.readValue(resp.getData(), Call.CallOutput.class);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " call result, status: {}, blk: {}",
                        callOutput.getStatus(),
                        callOutput.getCurrentBlockNumber());
            }

            if (StatusCode.Success.equals(callOutput.getStatus())) {
                response.setErrorCode(BCOSStatusCode.Success);
                response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
                response.setResult(FunctionUtility.decodeOutput(callOutput.getOutput()));
            } else {
                response.setErrorCode(BCOSStatusCode.CallNotSuccessStatus);
                response.setErrorMessage(StatusCode.getStatusMessage(callOutput.getStatus()));
            }

        } catch (BCOSStubException e) {
            logger.warn(" e: {}", e);
            throw new TransactionException(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.warn(" e: {}", e);
            throw new TransactionException(
                    BCOSStatusCode.UnclassifiedError, " errorMessage: " + e.getMessage());
        }

        if (logger.isTraceEnabled()) {
            logger.trace(
                    " errorCode: {}, errorMessage: {}, output: {}",
                    response.getErrorCode(),
                    response.getErrorMessage(),
                    response.getResult());
        }

        return response;
    }

    @Override
    public void asyncCall(
            TransactionContext<TransactionRequest> request,
            Connection connection,
            Callback callback) {

        try {
            TransactionResponse transactionResponse = call(request, connection);
            callback.onTransactionResponse(null, transactionResponse);
        } catch (TransactionException e) {
            callback.onTransactionResponse(e, null);
        }
    }

    @Override
    public TransactionResponse sendTransaction(
            TransactionContext<TransactionRequest> request, Connection connection)
            throws TransactionException {

        class CallAndSendTxResponseCallback implements Callback {

            public CallAndSendTxResponseCallback() {
                try {
                    this.semaphore.acquire();
                } catch (InterruptedException e) {
                    logger.error(" e: {}", e);
                    Thread.currentThread().interrupt();
                }
            }

            private Semaphore semaphore = new Semaphore(1, true);
            private TransactionException transactionException;
            private TransactionResponse transactionResponse;

            public Semaphore getSemaphore() {
                return semaphore;
            }

            public void setSemaphore(Semaphore semaphore) {
                this.semaphore = semaphore;
            }

            public TransactionException getTransactionException() {
                return transactionException;
            }

            public void setTransactionException(TransactionException transactionException) {
                this.transactionException = transactionException;
            }

            public TransactionResponse getTransactionResponse() {
                return transactionResponse;
            }

            public void setTransactionResponse(TransactionResponse transactionResponse) {
                this.transactionResponse = transactionResponse;
            }

            @Override
            public void onTransactionResponse(
                    TransactionException transactionException,
                    TransactionResponse transactionResponse) {
                this.transactionException = transactionException;
                this.transactionResponse = transactionResponse;
                this.getSemaphore().release();
            }
        }

        CallAndSendTxResponseCallback callAndSendTxResponseCallback =
                new CallAndSendTxResponseCallback();
        asyncSendTransaction(request, connection, callAndSendTxResponseCallback);

        try {
            callAndSendTxResponseCallback.getSemaphore().acquire();
        } catch (InterruptedException e) {
            logger.error(" e: {}", e);
            Thread.currentThread().interrupt();
        }

        if (callAndSendTxResponseCallback.getTransactionException() != null) {
            throw callAndSendTxResponseCallback.getTransactionException();
        }

        return callAndSendTxResponseCallback.getTransactionResponse();
    }

    @Override
    public void asyncSendTransaction(
            TransactionContext<TransactionRequest> request,
            Connection connection,
            Callback callback) {

        TransactionResponse transactionResponse = new TransactionResponse();

        try {
            ResourceInfo resourceInfo = request.getResourceInfo();
            Map<Object, Object> properties = resourceInfo.getProperties();
            // input validation
            checkRequest(request);
            checkProperties(resourceInfo.getName(), properties);

            // contractAddress
            String contractAddress = (String) properties.get(resourceInfo.getName());
            // groupId
            Integer groupId = (Integer) properties.get(BCOSConstant.BCOS_RESOURCEINFO_GROUP_ID);
            // chainId
            Integer chainId = (Integer) properties.get(BCOSConstant.BCOS_RESOURCEINFO_CHAIN_ID);

            long blockNumber = request.getBlockHeaderManager().getBlockNumber();

            // BCOSAccount to get credentials to sign the transaction
            BCOSAccount bcosAccount = (BCOSAccount) request.getAccount();
            Credentials credentials = bcosAccount.getCredentials();

            // Function object
            Function function =
                    FunctionUtility.newFunction(
                            request.getData().getMethod(), request.getData().getArgs());

            if (logger.isDebugEnabled()) {
                logger.debug(
                        " contractAddress: {}, blockNumber: {}, method: {}, args: {}",
                        contractAddress,
                        blockNumber,
                        request.getData().getMethod(),
                        request.getData().getArgs());
            }

            // get signed transaction hex string
            String signTx =
                    SignTransaction.sign(
                            credentials,
                            contractAddress,
                            BigInteger.valueOf(groupId),
                            BigInteger.valueOf(chainId),
                            BigInteger.valueOf(blockNumber),
                            FunctionEncoder.encode(function));

            TransactionParams transaction = new TransactionParams(request.getData(), signTx);
            Request req =
                    requestBuilder(
                            BCOSRequestType.SEND_TRANSACTION,
                            objectMapper.writeValueAsBytes(transaction));

            connection.asyncSend(
                    req,
                    new Connection.Callback() {
                        @Override
                        public void onResponse(Response response) {
                            try {
                                if (response.getErrorCode() != BCOSStatusCode.Success) {
                                    throw new BCOSStubException(
                                            response.getErrorCode(), response.getErrorMessage());
                                }

                                TransactionReceipt receipt =
                                        objectMapper.readValue(
                                                response.getData(), TransactionReceipt.class);

                                if (receipt.isStatusOK()) {
                                    verifyTransactionProof(
                                            receipt.getBlockNumber().longValue(),
                                            receipt.getTransactionHash(),
                                            request.getBlockHeaderManager(),
                                            receipt);

                                    transactionResponse.setBlockNumber(
                                            receipt.getBlockNumber().longValue());
                                    transactionResponse.setHash(receipt.getTransactionHash());
                                    transactionResponse.setResult(
                                            FunctionUtility.decodeOutput(receipt));
                                    transactionResponse.setErrorCode(BCOSStatusCode.Success);
                                    transactionResponse.setErrorMessage(
                                            BCOSStatusCode.getStatusMessage(
                                                    BCOSStatusCode.Success));
                                } else {
                                    transactionResponse.setErrorCode(
                                            BCOSStatusCode.SendTransactionNotSuccessStatus);
                                    transactionResponse.setErrorMessage(
                                            StatusCode.getStatusMessage(receipt.getStatus()));
                                }
                                callback.onTransactionResponse(null, transactionResponse);
                            } catch (BCOSStubException e) {
                                logger.warn(" e: {}", e);
                                callback.onTransactionResponse(
                                        new TransactionException(e.getErrorCode(), e.getMessage()),
                                        null);
                            } catch (Exception e) {
                                logger.warn(" e: {}", e);
                                callback.onTransactionResponse(
                                        new TransactionException(
                                                BCOSStatusCode.UnclassifiedError,
                                                " errorMessage: " + e.getMessage()),
                                        null);
                            }
                        }
                    });
        } catch (BCOSStubException e) {
            logger.warn(" e: {}", e);
            callback.onTransactionResponse(
                    new TransactionException(e.getErrorCode(), e.getMessage()), null);
        } catch (Exception e) {
            logger.warn(" e: {}", e);
            callback.onTransactionResponse(
                    new TransactionException(
                            BCOSStatusCode.UnclassifiedError, " errorMessage: " + e.getMessage()),
                    null);
        }
    }

    @Override
    public long getBlockNumber(Connection connection) {
        Request req = requestBuilder(BCOSRequestType.GET_BLOCK_NUMBER, "");
        Response resp = connection.send(req);

        // Returns an invalid value to indicate that the function performed incorrectly
        if (resp.getErrorCode() != 0) {
            logger.warn(
                    " errorCode: {},  errorMessage: {}",
                    resp.getErrorCode(),
                    resp.getErrorMessage());
            return -1;
        }

        BigInteger blockNumber = new BigInteger(resp.getData());
        logger.debug(" blockNumber: {}", blockNumber);
        return blockNumber.longValue();
    }

    @Override
    public byte[] getBlockHeader(long number, Connection connection) {
        Request request =
                requestBuilder(
                        BCOSRequestType.GET_BLOCK_HEADER, BigInteger.valueOf(number).toByteArray());
        Response response = connection.send(request);
        if (response.getErrorCode() != 0) {
            logger.warn(
                    " errorCode: {},  errorMessage: {}",
                    response.getErrorCode(),
                    response.getErrorMessage());
            return null;
        }

        return response.getData();
    }

    /**
     * @param transactionHash
     * @param connection
     * @return
     * @throws IOException
     */
    public TransactionProof requestTransactionProof(String transactionHash, Connection connection)
            throws IOException, BCOSStubException {

        Request request = requestBuilder(BCOSRequestType.GET_TRANSACTION_PROOF, transactionHash);
        Response resp = connection.send(request);
        if (resp.getErrorCode() != BCOSStatusCode.Success) {
            throw new BCOSStubException(resp.getErrorCode(), resp.getErrorMessage());
        }

        TransactionProof transactionProof =
                objectMapper.readValue(resp.getData(), TransactionProof.class);

        logger.debug(
                " transactionHash: {}, transactionProof: {}", transactionHash, transactionProof);

        return transactionProof;
    }

    /**
     * @param blockNumber
     * @param blockHeaderManager
     * @param transactionProof
     * @throws BCOSStubException
     */
    public void verifyTransactionProof(
            long blockNumber,
            String hash,
            BlockHeaderManager blockHeaderManager,
            TransactionProof transactionProof)
            throws BCOSStubException {
        // fetch block header
        byte[] bytesBlockHeader = blockHeaderManager.getBlockHeader(blockNumber);
        if (Objects.isNull(bytesBlockHeader) || bytesBlockHeader.length == 0) {
            throw new BCOSStubException(
                    BCOSStatusCode.FetchBlockHeaderFailed,
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.FetchBlockHeaderFailed)
                            + ", blockNumber: "
                            + blockNumber);
        }

        // decode block header
        BlockHeader blockHeader = decodeBlockHeader(bytesBlockHeader);
        if (Objects.isNull(blockHeader)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidEncodedBlockHeader,
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.InvalidEncodedBlockHeader)
                            + ", blockNumber: "
                            + blockNumber);
        }

        // verify transaction
        if (!MerkleProofUtility.verifyTransactionReceipt(
                blockHeader.getReceiptRoot(), transactionProof.getReceiptAndProof())) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionReceiptProofVerifyFailed,
                    BCOSStatusCode.getStatusMessage(
                                    BCOSStatusCode.TransactionReceiptProofVerifyFailed)
                            + ", hash="
                            + hash);
        }

        // verify transaction
        if (!MerkleProofUtility.verifyTransaction(
                blockHeader.getTransactionRoot(), transactionProof.getTransAndProof())) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionProofVerifyFailed,
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.TransactionProofVerifyFailed)
                            + ", hash="
                            + hash);
        }
    }

    /**
     * @param blockNumber
     * @param hash
     * @param blockHeaderManager
     * @param transactionReceipt
     * @throws BCOSStubException
     */
    public void verifyTransactionProof(
            long blockNumber,
            String hash,
            BlockHeaderManager blockHeaderManager,
            TransactionReceipt transactionReceipt)
            throws BCOSStubException {
        // fetch block header
        byte[] bytesBlockHeader = blockHeaderManager.getBlockHeader(blockNumber);
        if (Objects.isNull(bytesBlockHeader) || bytesBlockHeader.length == 0) {
            throw new BCOSStubException(
                    BCOSStatusCode.FetchBlockHeaderFailed,
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.FetchBlockHeaderFailed)
                            + ", blockNumber: "
                            + blockNumber);
        }

        // decode block header
        BlockHeader blockHeader = decodeBlockHeader(bytesBlockHeader);
        if (Objects.isNull(blockHeader)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidEncodedBlockHeader,
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.InvalidEncodedBlockHeader)
                            + ", blockNumber: "
                            + blockNumber);
        }

        // verify transaction
        if (!MerkleProofUtility.verifyTransactionReceipt(
                blockHeader.getReceiptRoot(),
                transactionReceipt,
                transactionReceipt.getReceiptProof())) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionReceiptProofVerifyFailed,
                    BCOSStatusCode.getStatusMessage(
                                    BCOSStatusCode.TransactionReceiptProofVerifyFailed)
                            + ", hash="
                            + hash);
        }

        // verify transaction
        if (!MerkleProofUtility.verifyTransaction(
                transactionReceipt.getTransactionHash(),
                transactionReceipt.getTransactionIndex(),
                blockHeader.getTransactionRoot(),
                transactionReceipt.getTxProof())) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionProofVerifyFailed,
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.TransactionProofVerifyFailed)
                            + ", hash="
                            + hash);
        }
    }

    @Override
    public VerifiedTransaction getVerifiedTransaction(
            String transactionHash,
            long blockNumber,
            BlockHeaderManager blockHeaderManager,
            Connection connection) {
        try {
            // get transaction proof
            TransactionProof transactionProof =
                    requestTransactionProof(transactionHash, connection);
            TransactionReceipt receipt =
                    transactionProof.getReceiptAndProof().getTransactionReceipt();
            Transaction transaction = transactionProof.getTransAndProof().getTransaction();
            if (blockNumber != receipt.getBlockNumber().longValue()) {
                logger.warn(
                        " invalid blockNumber, blockNumber: {}, receipt blockNumber: {}",
                        blockNumber,
                        receipt.getBlockNumber());
                blockNumber = receipt.getBlockNumber().longValue();
            }

            verifyTransactionProof(
                    blockNumber, transactionHash, blockHeaderManager, transactionProof);

            TransactionRequest transactionRequest = new TransactionRequest();
            /** decode input args from input */
            transactionRequest.setArgs(FunctionUtility.decodeInput(receipt));

            TransactionResponse transactionResponse = new TransactionResponse();
            transactionResponse.setHash(transactionHash);
            transactionResponse.setBlockNumber(receipt.getBlockNumber().longValue());
            /** decode output from output */
            transactionResponse.setResult(FunctionUtility.decodeOutput(receipt));

            /** set error code and error message info */
            transactionResponse.setErrorMessage(StatusCode.getStatusMessage(receipt.getStatus()));
            BigInteger statusCode = new BigInteger(Numeric.cleanHexPrefix(receipt.getStatus()), 16);
            transactionResponse.setErrorCode(statusCode.intValue());

            VerifiedTransaction verifiedTransaction =
                    new VerifiedTransaction(
                            blockNumber,
                            transactionHash,
                            receipt.getTo(),
                            transactionRequest,
                            transactionResponse);

            logger.trace(" VerifiedTransaction: {}", verifiedTransaction);

            return verifiedTransaction;
        } catch (Exception e) {
            logger.warn(" transactionHash: {}, Exception: {}", transactionHash, e);
            return null;
        }
    }

    /**
     * @param name
     * @param properties
     * @throws BCOSStubException
     */
    public void checkProperties(String name, Map<Object, Object> properties)
            throws BCOSStubException {
        try {
            // contractAddress
            String contractAddress = (String) properties.get(name);
            if (Objects.isNull(contractAddress)) {
                throw new BCOSStubException(
                        BCOSStatusCode.InvalidParameter,
                        " Not found contract address, resource: " + name);
            }

            Integer groupId = (Integer) properties.get(BCOSConstant.BCOS_RESOURCEINFO_GROUP_ID);
            if (Objects.isNull(groupId)) {
                throw new BCOSStubException(
                        BCOSStatusCode.InvalidParameter, " Not found groupId, resource: " + name);
            }

            Integer chainId = (Integer) properties.get(BCOSConstant.BCOS_RESOURCEINFO_CHAIN_ID);
            if (Objects.isNull(chainId)) {
                throw new BCOSStubException(
                        BCOSStatusCode.InvalidParameter, " Not found chainId, resource: " + name);
            }
        } catch (BCOSStubException e) {
            throw e;
        } catch (Exception e) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter, "errorMessage: " + e.getMessage());
        }
    }

    /**
     * check request field valid
     *
     * @param request
     * @throws BCOSStubException
     */
    public void checkRequest(TransactionContext<TransactionRequest> request)
            throws BCOSStubException {
        if (Objects.isNull(request)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter, "TransactionContext is null");
        }

        if (Objects.isNull(request.getAccount())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "Account is null");
        }

        if (Objects.isNull(request.getBlockHeaderManager())) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter, "BlockHeaderManager is null");
        }

        if (Objects.isNull(request.getResourceInfo())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "ResourceInfo is null");
        }

        if (Objects.isNull(request.getData())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "Data is null");
        }

        if (Objects.isNull(request.getData().getMethod())
                || "".equals(request.getData().getMethod())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "Method is null");
        }
    }
}
