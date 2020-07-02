package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.abi.ABIObject;
import com.webank.wecross.stub.bcos.abi.ABIObjectJSONWrapper;
import com.webank.wecross.stub.bcos.abi.Contract;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.*;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.custom.CommandHandler;
import com.webank.wecross.stub.bcos.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import com.webank.wecross.stub.bcos.verify.MerkleValidation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Semaphore;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.generated.Uint256;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionDecoder;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Driver implementation for BCOS */
public class BCOSDriver implements Driver {

    private static final Logger logger = LoggerFactory.getLogger(BCOSDriver.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private CommandHandlerDispatcher commandHandlerDispatcher;

    private AsyncCnsService asyncCnsService = new AsyncCnsService();

    ABIObjectJSONWrapper abiFactory = new ABIObjectJSONWrapper();

    private Map<String, String> abiMap = new HashMap<>();

    public BCOSDriver() {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
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

    @Override
    public TransactionResponse call(
            TransactionContext<TransactionRequest> request, Connection connection)
            throws TransactionException {

        CallAndSendTxResponseCallback callAndSendTxResponseCallback =
                new CallAndSendTxResponseCallback();
        asyncCall(request, connection, callAndSendTxResponseCallback);

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
    public void asyncCallByProxy(
            TransactionContext<TransactionRequest> request,
            Connection connection,
            Callback callback) {

        TransactionResponse transactionResponse = new TransactionResponse();

        try {
            Map<String, String> properties = connection.getProperties();

            // input validation
            checkRequest(request);
            checkProperties(properties);

            String contractAddress = properties.get(BCOSConstant.BCOS_PROXY_NAME);
            String path = request.getData().getPath();
            String name = path.split("\\.")[2];

            // query abi
            asyncCnsService.queryABI(
                    name,
                    request.getAccount(),
                    connection,
                    (queryABIException, abi) -> {
                        try {
                            if (Objects.nonNull(queryABIException)) {
                                throw new BCOSStubException(
                                        BCOSStatusCode.QueryAbiFailed,
                                        queryABIException.getMessage());
                            }

                            if (abi == null) {
                                throw new BCOSStubException(
                                        BCOSStatusCode.QueryAbiFailed, "abi is null");
                            }

                            // encode
                            String encodedArgs;
                            String[] args = request.getData().getArgs();
                            String method = request.getData().getMethod();
                            Contract contract =
                                    abiFactory.loadABIFile(
                                            new ByteArrayInputStream(abi.getBytes()));
                            List<com.webank.wecross.stub.bcos.abi.Function> functions =
                                    contract.getFunctions().get(method);
                            if (Objects.isNull(functions) || functions.isEmpty()) {
                                throw new BCOSStubException(
                                        BCOSStatusCode.MethodNotExist, "method not found in abi");
                            }

                            // Overloading is not supported ???
                            ABIObject inputObj = functions.get(0).getInput();

                            if (Objects.isNull(args)) {
                                encodedArgs = "";
                            } else {
                                ABIObject encodedObj =
                                        abiFactory.encode(inputObj, Arrays.asList(args));
                                encodedArgs = encodedObj.encode();
                            }
                            String transactionID =
                                    (String)
                                            request.getData()
                                                    .getOptions()
                                                    .get(BCOSConstant.TRANSACTION_ID);
                            String id = Objects.isNull(transactionID) ? "0" : transactionID;
                            Function function =
                                    new Function(
                                            "constantCall",
                                            Arrays.<Type>asList(
                                                    new org.fisco.bcos.web3j.abi.datatypes
                                                            .Utf8String(id),
                                                    new org.fisco.bcos.web3j.abi.datatypes
                                                            .Utf8String(path),
                                                    new org.fisco.bcos.web3j.abi.datatypes
                                                            .Utf8String(
                                                            abiFactory.getSigbyMethod(method, abi)),
                                                    new org.fisco.bcos.web3j.abi.datatypes
                                                            .DynamicBytes(
                                                            Numeric.hexStringToByteArray(
                                                                    encodedArgs))),
                                            Collections.<TypeReference<?>>emptyList());

                            // BCOSAccount to get credentials to sign the transaction
                            BCOSAccount bcosAccount = (BCOSAccount) request.getAccount();
                            Credentials credentials = bcosAccount.getCredentials();

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        " name:{}, address: {}, method: {}, args: {}",
                                        BCOSConstant.BCOS_PROXY_NAME,
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
                                    RequestFactory.requestBuilder(
                                            BCOSRequestType.CALL,
                                            objectMapper.writeValueAsBytes(transaction));

                            connection.asyncSend(
                                    req,
                                    connectionResponse -> {
                                        try {
                                            if (connectionResponse.getErrorCode()
                                                    != BCOSStatusCode.Success) {
                                                throw new BCOSStubException(
                                                        connectionResponse.getErrorCode(),
                                                        connectionResponse.getErrorMessage());
                                            }

                                            Call.CallOutput callOutput =
                                                    objectMapper.readValue(
                                                            connectionResponse.getData(),
                                                            Call.CallOutput.class);

                                            if (logger.isDebugEnabled()) {
                                                logger.debug(
                                                        " call result, status: {}, blk: {}",
                                                        callOutput.getStatus(),
                                                        callOutput.getCurrentBlockNumber());
                                            }

                                            if (StatusCode.Success.equals(callOutput.getStatus())) {
                                                transactionResponse.setErrorCode(
                                                        BCOSStatusCode.Success);
                                                transactionResponse.setErrorMessage(
                                                        BCOSStatusCode.getStatusMessage(
                                                                BCOSStatusCode.Success));

                                                ABIObject outputObj = functions.get(0).getOutput();

                                                // decode outputs
                                                String output =
                                                        callOutput.getOutput().substring(130);
                                                transactionResponse.setResult(
                                                        abiFactory
                                                                .decode(outputObj, output)
                                                                .toArray(new String[0]));
                                            } else {
                                                transactionResponse.setErrorCode(
                                                        BCOSStatusCode.CallNotSuccessStatus);
                                                transactionResponse.setErrorMessage(
                                                        StatusCode.getStatusMessage(
                                                                callOutput.getStatus()));
                                            }

                                            callback.onTransactionResponse(
                                                    null, transactionResponse);

                                        } catch (BCOSStubException e) {
                                            logger.warn(" e: {}", e);
                                            callback.onTransactionResponse(
                                                    new TransactionException(
                                                            e.getErrorCode(), e.getMessage()),
                                                    null);
                                        } catch (Exception e) {
                                            logger.warn(" e: {}", e);
                                            callback.onTransactionResponse(
                                                    new TransactionException(
                                                            BCOSStatusCode.UnclassifiedError,
                                                            " errorMessage: " + e.getMessage()),
                                                    null);
                                        }
                                    });

                        } catch (BCOSStubException bse) {
                            logger.warn(" e: {}", bse);
                            callback.onTransactionResponse(
                                    new TransactionException(bse.getErrorCode(), bse.getMessage()),
                                    null);
                        } catch (Exception e) {
                            logger.warn(" e: {}", e);
                            callback.onTransactionResponse(
                                    new TransactionException(
                                            BCOSStatusCode.UnclassifiedError, e.getMessage()),
                                    null);
                        }
                    });

        } catch (BCOSStubException e) {
            logger.warn(" e: {}", e);
            callback.onTransactionResponse(
                    new TransactionException(e.getErrorCode(), e.getMessage()), null);
        }
    }

    @Override
    public void asyncCall(
            TransactionContext<TransactionRequest> request,
            Connection connection,
            Callback callback) {
        TransactionResponse transactionResponse = new TransactionResponse();

        try {
            Map<String, String> properties = connection.getProperties();

            // input validation
            checkRequest(request);
            checkProperties(properties);

            String contractAddress = properties.get(BCOSConstant.BCOS_PROXY_NAME);

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
                        BCOSConstant.BCOS_PROXY_NAME,
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
                    RequestFactory.requestBuilder(
                            BCOSRequestType.CALL, objectMapper.writeValueAsBytes(transaction));

            connection.asyncSend(
                    req,
                    connectionResponse -> {
                        try {
                            if (connectionResponse.getErrorCode() != BCOSStatusCode.Success) {
                                throw new BCOSStubException(
                                        connectionResponse.getErrorCode(),
                                        connectionResponse.getErrorMessage());
                            }

                            Call.CallOutput callOutput =
                                    objectMapper.readValue(
                                            connectionResponse.getData(), Call.CallOutput.class);

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        " call result, status: {}, blk: {}",
                                        callOutput.getStatus(),
                                        callOutput.getCurrentBlockNumber());
                            }

                            if (StatusCode.Success.equals(callOutput.getStatus())) {
                                transactionResponse.setErrorCode(BCOSStatusCode.Success);
                                transactionResponse.setErrorMessage(
                                        BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
                                transactionResponse.setResult(
                                        FunctionUtility.decodeOutput(callOutput.getOutput()));
                            } else {
                                transactionResponse.setErrorCode(
                                        BCOSStatusCode.CallNotSuccessStatus);
                                transactionResponse.setErrorMessage(
                                        StatusCode.getStatusMessage(callOutput.getStatus()));
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
    public TransactionResponse sendTransaction(
            TransactionContext<TransactionRequest> request, Connection connection)
            throws TransactionException {

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
            Map<String, String> properties = connection.getProperties();

            // input validation
            checkRequest(request);
            checkProperties(properties);

            // contractAddress
            String contractAddress = properties.get(BCOSConstant.BCOS_PROXY_NAME);
            // groupId
            int groupId = Integer.parseInt(properties.get(BCOSConstant.BCOS_GROUP_ID));
            // chainId
            int chainId = Integer.parseInt(properties.get(BCOSConstant.BCOS_CHAIN_ID));

            request.getBlockHeaderManager()
                    .asyncGetBlockNumber(
                            (blockNumberException, blockNumber) -> {
                                if (Objects.nonNull(blockNumberException)) {
                                    callback.onTransactionResponse(
                                            new TransactionException(
                                                    BCOSStatusCode.HandleGetBlockNumberFailed,
                                                    blockNumberException.getMessage()),
                                            null);
                                    return;
                                }
                                // BCOSAccount to get credentials to sign the transaction
                                BCOSAccount bcosAccount = (BCOSAccount) request.getAccount();
                                Credentials credentials = bcosAccount.getCredentials();

                                // Function object
                                Function function =
                                        FunctionUtility.newFunction(
                                                request.getData().getMethod(),
                                                request.getData().getArgs());

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

                                TransactionParams transaction =
                                        new TransactionParams(request.getData(), signTx);
                                Request req;
                                try {
                                    req =
                                            RequestFactory.requestBuilder(
                                                    BCOSRequestType.SEND_TRANSACTION,
                                                    objectMapper.writeValueAsBytes(transaction));
                                } catch (JsonProcessingException e) {
                                    callback.onTransactionResponse(
                                            new TransactionException(
                                                    BCOSStatusCode.UnclassifiedError,
                                                    " errorMessage: " + e.getMessage()),
                                            null);
                                    return;
                                }

                                connection.asyncSend(
                                        req,
                                        response -> {
                                            try {
                                                if (response.getErrorCode()
                                                        != BCOSStatusCode.Success) {
                                                    throw new BCOSStubException(
                                                            response.getErrorCode(),
                                                            response.getErrorMessage());
                                                }

                                                TransactionReceipt receipt =
                                                        objectMapper.readValue(
                                                                response.getData(),
                                                                TransactionReceipt.class);

                                                if (receipt.isStatusOK()) {
                                                    request.getBlockHeaderManager()
                                                            .asyncGetBlockHeader(
                                                                    receipt.getBlockNumber()
                                                                            .longValue(),
                                                                    (blockHeaderException,
                                                                            blockHeader) -> {
                                                                        try {
                                                                            if (Objects.nonNull(
                                                                                    blockHeaderException)) {
                                                                                callback
                                                                                        .onTransactionResponse(
                                                                                                new TransactionException(
                                                                                                        BCOSStatusCode
                                                                                                                .HandleGetBlockNumberFailed,
                                                                                                        blockHeaderException
                                                                                                                .getMessage()),
                                                                                                null);
                                                                                return;
                                                                            }
                                                                            MerkleValidation
                                                                                    merkleValidation =
                                                                                            new MerkleValidation();
                                                                            merkleValidation
                                                                                    .verifyTransactionReceiptProof(
                                                                                            receipt.getBlockNumber()
                                                                                                    .longValue(),
                                                                                            receipt
                                                                                                    .getTransactionHash(),
                                                                                            blockHeader,
                                                                                            receipt);

                                                                            transactionResponse
                                                                                    .setBlockNumber(
                                                                                            receipt.getBlockNumber()
                                                                                                    .longValue());
                                                                            transactionResponse
                                                                                    .setHash(
                                                                                            receipt
                                                                                                    .getTransactionHash());
                                                                            transactionResponse
                                                                                    .setResult(
                                                                                            FunctionUtility
                                                                                                    .decodeOutput(
                                                                                                            receipt));
                                                                            transactionResponse
                                                                                    .setErrorCode(
                                                                                            BCOSStatusCode
                                                                                                    .Success);
                                                                            transactionResponse
                                                                                    .setErrorMessage(
                                                                                            BCOSStatusCode
                                                                                                    .getStatusMessage(
                                                                                                            BCOSStatusCode
                                                                                                                    .Success));
                                                                            callback
                                                                                    .onTransactionResponse(
                                                                                            null,
                                                                                            transactionResponse);
                                                                            if (logger
                                                                                    .isDebugEnabled()) {
                                                                                logger.debug(
                                                                                        " hash: {}, response: {}",
                                                                                        receipt
                                                                                                .getTransactionHash(),
                                                                                        transactionResponse);
                                                                            }
                                                                        } catch (
                                                                                BCOSStubException
                                                                                        e) {
                                                                            logger.warn(
                                                                                    " e: {}", e);
                                                                            callback
                                                                                    .onTransactionResponse(
                                                                                            new TransactionException(
                                                                                                    e
                                                                                                            .getErrorCode(),
                                                                                                    e
                                                                                                            .getMessage()),
                                                                                            null);
                                                                        }
                                                                    });

                                                } else {
                                                    transactionResponse.setErrorCode(
                                                            BCOSStatusCode
                                                                    .SendTransactionNotSuccessStatus);
                                                    transactionResponse.setErrorMessage(
                                                            StatusCode.getStatusMessage(
                                                                    receipt.getStatus()));
                                                    callback.onTransactionResponse(
                                                            null, transactionResponse);
                                                }
                                            } catch (BCOSStubException e) {
                                                logger.warn(" e: {}", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                e.getErrorCode(), e.getMessage()),
                                                        null);
                                            } catch (Exception e) {
                                                logger.warn(" e: {}", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                BCOSStatusCode.UnclassifiedError,
                                                                " errorMessage: " + e.getMessage()),
                                                        null);
                                            }
                                        });
                            });

        } catch (BCOSStubException e) {
            logger.warn(" e: {}", e);
            callback.onTransactionResponse(
                    new TransactionException(e.getErrorCode(), e.getMessage()), null);
        }
    }

    @Override
    public void asyncSendTransactionByProxy(
            TransactionContext<TransactionRequest> request,
            Connection connection,
            Callback callback) {
        TransactionResponse transactionResponse = new TransactionResponse();

        try {
            Map<String, String> properties = connection.getProperties();

            // input validation
            checkRequest(request);
            checkProperties(properties);

            // contractAddress
            String contractAddress = properties.get(BCOSConstant.BCOS_PROXY_NAME);
            // groupId
            int groupId = Integer.parseInt(properties.get(BCOSConstant.BCOS_GROUP_ID));
            // chainId
            int chainId = Integer.parseInt(properties.get(BCOSConstant.BCOS_CHAIN_ID));

            request.getBlockHeaderManager()
                    .asyncGetBlockNumber(
                            (blockNumberException, blockNumber) -> {
                                if (Objects.nonNull(blockNumberException)) {
                                    callback.onTransactionResponse(
                                            new TransactionException(
                                                    BCOSStatusCode.HandleGetBlockNumberFailed,
                                                    blockNumberException.getMessage()),
                                            null);
                                    return;
                                }
                                // BCOSAccount to get credentials to sign the transaction
                                BCOSAccount bcosAccount = (BCOSAccount) request.getAccount();
                                Credentials credentials = bcosAccount.getCredentials();

                                String path = request.getData().getPath();
                                String name = path.split("\\.")[2];

                                // query abi
                                asyncCnsService.queryABI(
                                        name,
                                        request.getAccount(),
                                        connection,
                                        (queryABIException, abi) -> {
                                            try {
                                                if (Objects.nonNull(queryABIException)) {
                                                    throw new BCOSStubException(
                                                            BCOSStatusCode.QueryAbiFailed,
                                                            queryABIException.getMessage());
                                                }

                                                if (abi == null) {
                                                    throw new BCOSStubException(
                                                            BCOSStatusCode.QueryAbiFailed,
                                                            "abi is null");
                                                }

                                                // encode
                                                String encodedArgs;
                                                String[] args = request.getData().getArgs();
                                                String method = request.getData().getMethod();
                                                Contract contract =
                                                        abiFactory.loadABIFile(
                                                                new ByteArrayInputStream(
                                                                        abi.getBytes()));
                                                List<com.webank.wecross.stub.bcos.abi.Function>
                                                        functions =
                                                                contract.getFunctions().get(method);
                                                if (Objects.isNull(functions)
                                                        || functions.isEmpty()) {
                                                    throw new BCOSStubException(
                                                            BCOSStatusCode.MethodNotExist,
                                                            "method not found in abi");
                                                }
                                                ABIObject inputObj = functions.get(0).getInput();

                                                if (Objects.isNull(args)) {
                                                    encodedArgs = "";
                                                } else {
                                                    ABIObject encodedObj =
                                                            abiFactory.encode(
                                                                    inputObj, Arrays.asList(args));
                                                    encodedArgs = encodedObj.encode();
                                                }
                                                String transactionID =
                                                        (String)
                                                                request.getData()
                                                                        .getOptions()
                                                                        .get(
                                                                                BCOSConstant
                                                                                        .TRANSACTION_ID);
                                                String id =
                                                        Objects.isNull(transactionID)
                                                                ? "0"
                                                                : transactionID;

                                                String transactionSeq =
                                                        (String)
                                                                request.getData()
                                                                        .getOptions()
                                                                        .get(
                                                                                BCOSConstant
                                                                                        .TRANSACTION_SEQ);
                                                int seq =
                                                        Objects.isNull(transactionSeq)
                                                                ? 0
                                                                : Integer.parseInt(transactionSeq);

                                                String sig = abiFactory.getSigbyMethod(method, abi);

                                                Function function =
                                                        new Function(
                                                                "sendTransaction",
                                                                Arrays.<Type>asList(
                                                                        new org.fisco.bcos.web3j.abi
                                                                                .datatypes
                                                                                .Utf8String(id),
                                                                        new Uint256(seq),
                                                                        new org.fisco.bcos.web3j.abi
                                                                                .datatypes
                                                                                .Utf8String(path),
                                                                        new org.fisco.bcos.web3j.abi
                                                                                .datatypes
                                                                                .Utf8String(sig),
                                                                        new org.fisco.bcos.web3j.abi
                                                                                .datatypes
                                                                                .DynamicBytes(
                                                                                Numeric
                                                                                        .hexStringToByteArray(
                                                                                                encodedArgs))),
                                                                Collections
                                                                        .<TypeReference<?>>
                                                                                emptyList());

                                                if (logger.isDebugEnabled()) {
                                                    logger.debug(
                                                            " contractAddress: {}, blockNumber: {}, method: {}, args: {}",
                                                            contractAddress,
                                                            blockNumber,
                                                            request.getData().getMethod(),
                                                            request.getData().getArgs());
                                                }

                                                String encodedAbi =
                                                        FunctionEncoder.encode(function);
                                                // get signed transaction hex string
                                                String signTx =
                                                        SignTransaction.sign(
                                                                credentials,
                                                                contractAddress,
                                                                BigInteger.valueOf(groupId),
                                                                BigInteger.valueOf(chainId),
                                                                BigInteger.valueOf(blockNumber),
                                                                encodedAbi);

                                                TransactionParams transaction =
                                                        new TransactionParams(
                                                                request.getData(), signTx);
                                                Request req =
                                                        RequestFactory.requestBuilder(
                                                                BCOSRequestType.SEND_TRANSACTION,
                                                                objectMapper.writeValueAsBytes(
                                                                        transaction));

                                                connection.asyncSend(
                                                        req,
                                                        response -> {
                                                            try {
                                                                if (response.getErrorCode()
                                                                        != BCOSStatusCode.Success) {
                                                                    throw new BCOSStubException(
                                                                            response.getErrorCode(),
                                                                            response
                                                                                    .getErrorMessage());
                                                                }

                                                                TransactionReceipt receipt =
                                                                        objectMapper.readValue(
                                                                                response.getData(),
                                                                                TransactionReceipt
                                                                                        .class);

                                                                if (receipt.isStatusOK()) {
                                                                    request.getBlockHeaderManager()
                                                                            .asyncGetBlockHeader(
                                                                                    receipt.getBlockNumber()
                                                                                            .longValue(),
                                                                                    (blockHeaderException,
                                                                                            blockHeader) -> {
                                                                                        try {
                                                                                            if (Objects
                                                                                                    .nonNull(
                                                                                                            blockHeaderException)) {
                                                                                                callback
                                                                                                        .onTransactionResponse(
                                                                                                                new TransactionException(
                                                                                                                        BCOSStatusCode
                                                                                                                                .HandleGetBlockNumberFailed,
                                                                                                                        blockHeaderException
                                                                                                                                .getMessage()),
                                                                                                                null);
                                                                                                return;
                                                                                            }
                                                                                            MerkleValidation
                                                                                                    merkleValidation =
                                                                                                            new MerkleValidation();
                                                                                            merkleValidation
                                                                                                    .verifyTransactionReceiptProof(
                                                                                                            receipt.getBlockNumber()
                                                                                                                    .longValue(),
                                                                                                            receipt
                                                                                                                    .getTransactionHash(),
                                                                                                            blockHeader,
                                                                                                            receipt);

                                                                                            transactionResponse
                                                                                                    .setBlockNumber(
                                                                                                            receipt.getBlockNumber()
                                                                                                                    .longValue());
                                                                                            transactionResponse
                                                                                                    .setHash(
                                                                                                            receipt
                                                                                                                    .getTransactionHash());
                                                                                            // decode
                                                                                            String
                                                                                                    output =
                                                                                                            receipt.getOutput()
                                                                                                                    .substring(
                                                                                                                            130);

                                                                                            ABIObject
                                                                                                    outputObj =
                                                                                                            functions
                                                                                                                    .get(
                                                                                                                            0)
                                                                                                                    .getOutput();

                                                                                            transactionResponse
                                                                                                    .setResult(
                                                                                                            abiFactory
                                                                                                                    .decode(
                                                                                                                            outputObj,
                                                                                                                            output)
                                                                                                                    .toArray(
                                                                                                                            new String
                                                                                                                                    [0]));

                                                                                            transactionResponse
                                                                                                    .setErrorCode(
                                                                                                            BCOSStatusCode
                                                                                                                    .Success);
                                                                                            transactionResponse
                                                                                                    .setErrorMessage(
                                                                                                            BCOSStatusCode
                                                                                                                    .getStatusMessage(
                                                                                                                            BCOSStatusCode
                                                                                                                                    .Success));
                                                                                            callback
                                                                                                    .onTransactionResponse(
                                                                                                            null,
                                                                                                            transactionResponse);
                                                                                            if (logger
                                                                                                    .isDebugEnabled()) {
                                                                                                logger
                                                                                                        .debug(
                                                                                                                " hash: {}, response: {}",
                                                                                                                receipt
                                                                                                                        .getTransactionHash(),
                                                                                                                transactionResponse);
                                                                                            }
                                                                                        } catch (
                                                                                                BCOSStubException
                                                                                                        e) {
                                                                                            logger
                                                                                                    .warn(
                                                                                                            " e: {}",
                                                                                                            e);
                                                                                            callback
                                                                                                    .onTransactionResponse(
                                                                                                            new TransactionException(
                                                                                                                    e
                                                                                                                            .getErrorCode(),
                                                                                                                    e
                                                                                                                            .getMessage()),
                                                                                                            null);
                                                                                        }
                                                                                    });

                                                                } else {
                                                                    transactionResponse
                                                                            .setErrorCode(
                                                                                    BCOSStatusCode
                                                                                            .SendTransactionNotSuccessStatus);
                                                                    transactionResponse
                                                                            .setErrorMessage(
                                                                                    StatusCode
                                                                                            .getStatusMessage(
                                                                                                    receipt
                                                                                                            .getStatus()));
                                                                    callback.onTransactionResponse(
                                                                            null,
                                                                            transactionResponse);
                                                                }
                                                            } catch (BCOSStubException e) {
                                                                logger.warn(" e: {}", e);
                                                                callback.onTransactionResponse(
                                                                        new TransactionException(
                                                                                e.getErrorCode(),
                                                                                e.getMessage()),
                                                                        null);
                                                            } catch (Exception e) {
                                                                logger.warn(" e: {}", e);
                                                                callback.onTransactionResponse(
                                                                        new TransactionException(
                                                                                BCOSStatusCode
                                                                                        .UnclassifiedError,
                                                                                e.getMessage()),
                                                                        null);
                                                            }
                                                        });

                                            } catch (BCOSStubException e) {
                                                logger.warn(" e: {}", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                e.getErrorCode(), e.getMessage()),
                                                        null);
                                            } catch (Exception e) {
                                                logger.warn(" e: {}", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                BCOSStatusCode.UnclassifiedError,
                                                                e.getMessage()),
                                                        null);
                                            }
                                        });
                            });

        } catch (BCOSStubException e) {
            logger.warn(" e: {}", e);
            callback.onTransactionResponse(
                    new TransactionException(e.getErrorCode(), e.getMessage()), null);
        }
    }

    @Override
    public void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback) {
        Request request = RequestFactory.requestBuilder(BCOSRequestType.GET_BLOCK_NUMBER, "");

        connection.asyncSend(
                request,
                response -> {
                    // Returns an invalid value to indicate that the function performed incorrectly
                    if (response.getErrorCode() != 0) {
                        logger.warn(
                                " errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                        callback.onResponse(new Exception(response.getErrorMessage()), -1);
                    } else {
                        BigInteger blockNumber = new BigInteger(response.getData());
                        logger.debug(" blockNumber: {}", blockNumber);
                        callback.onResponse(null, blockNumber.longValue());
                    }
                });
    }

    @Override
    public void asyncGetBlockHeader(
            long blockNumber, Connection connection, GetBlockHeaderCallback callback) {
        Request request =
                RequestFactory.requestBuilder(
                        BCOSRequestType.GET_BLOCK_HEADER,
                        BigInteger.valueOf(blockNumber).toByteArray());
        connection.asyncSend(
                request,
                response -> {
                    if (response.getErrorCode() != 0) {
                        logger.warn(
                                " errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());
                        callback.onResponse(new Exception(response.getErrorMessage()), null);
                    } else {
                        callback.onResponse(null, response.getData());
                    }
                });
    }

    @Override
    public void asyncGetVerifiedTransaction(
            String transactionHash,
            long blockNumber,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            GetVerifiedTransactionCallback callback) {
        try {
            // get transaction proof
            TransactionProof transactionProof =
                    requestTransactionProof(transactionHash, connection);
            TransactionReceipt receipt =
                    transactionProof.getReceiptAndProof().getTransactionReceipt();
            if (blockNumber != receipt.getBlockNumber().longValue()) {
                logger.warn(
                        " invalid blockNumber, blockNumber: {}, receipt blockNumber: {}",
                        blockNumber,
                        receipt.getBlockNumber());
                blockNumber = receipt.getBlockNumber().longValue();
            }

            long finalBlockNumber = blockNumber;
            MerkleValidation merkleValidation = new MerkleValidation();
            merkleValidation.verifyTransactionProof(
                    blockNumber,
                    transactionHash,
                    blockHeaderManager,
                    transactionProof,
                    verifyException -> {
                        if (Objects.nonNull(verifyException)) {
                            callback.onResponse(
                                    new Exception(
                                            "verifying transaction failed "
                                                    + verifyException.getMessage()),
                                    null);
                            return;
                        }

                        TransactionRequest transactionRequest = new TransactionRequest();
                        /** decode input args from input */
                        transactionRequest.setArgs(FunctionUtility.decodeInput(receipt));

                        TransactionResponse transactionResponse = new TransactionResponse();
                        transactionResponse.setHash(transactionHash);
                        transactionResponse.setBlockNumber(receipt.getBlockNumber().longValue());
                        /** decode output from output */
                        transactionResponse.setResult(FunctionUtility.decodeOutput(receipt));

                        /** set error code and error message info */
                        transactionResponse.setErrorMessage(
                                StatusCode.getStatusMessage(receipt.getStatus()));
                        BigInteger statusCode =
                                new BigInteger(Numeric.cleanHexPrefix(receipt.getStatus()), 16);
                        transactionResponse.setErrorCode(statusCode.intValue());

                        VerifiedTransaction verifiedTransaction =
                                new VerifiedTransaction(
                                        finalBlockNumber,
                                        transactionHash,
                                        receipt.getTo(),
                                        transactionRequest,
                                        transactionResponse);

                        logger.trace(" VerifiedTransaction: {}", verifiedTransaction);
                        callback.onResponse(null, verifiedTransaction);
                    });
        } catch (Exception e) {
            logger.warn("transactionHash: {}", transactionHash, e);
            callback.onResponse(e, null);
        }
    }

    /**
     * @param transactionHash
     * @param connection
     * @return
     * @throws IOException
     */
    public TransactionProof requestTransactionProof(String transactionHash, Connection connection)
            throws IOException, BCOSStubException {

        Request request =
                RequestFactory.requestBuilder(
                        BCOSRequestType.GET_TRANSACTION_PROOF, transactionHash);
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

    @Override
    public void asyncCustomCommand(
            String command,
            Path path,
            Object[] args,
            Account account,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            CustomCommandCallback callback) {
        CommandHandler commandHandler = commandHandlerDispatcher.matchCommandHandler(command);
        if (Objects.isNull(commandHandler)) {
            callback.onResponse(new Exception("command not found"), null);
            return;
        }

        commandHandler.handle(
                path,
                args,
                account,
                blockHeaderManager,
                connection,
                abiMap,
                (error, response) -> {
                    callback.onResponse(error, response);

                    if (Objects.isNull(error)
                            && (BCOSConstant.CUSTOM_COMMAND_DEPLOY.equals(command)
                                    || BCOSConstant.CUSTOM_COMMAND_REGISTER.equals(command))) {
                        // add path into proxy contract
                        TransactionRequest request =
                                new TransactionRequest(
                                        BCOSConstant.PROXY_METHOD_ADDPATH,
                                        new String[] {path.toString()});
                        TransactionContext<TransactionRequest> context =
                                new TransactionContext<>(
                                        request, account, new ResourceInfo(), blockHeaderManager);
                        try {
                            TransactionResponse transactionResponse =
                                    sendTransaction(context, connection);
                            if (transactionResponse.getErrorCode() != 0) {
                                logger.warn(
                                        "setting path into proxy contract failed: {}",
                                        transactionResponse.getErrorMessage());
                            }
                        } catch (TransactionException e) {
                            logger.warn("setting path into proxy contract failed,", e);
                        }
                    }
                });
    }

    /**
     * @param properties
     * @throws BCOSStubException
     */
    public void checkProperties(Map<String, String> properties) throws BCOSStubException {
        if (!properties.containsKey(BCOSConstant.BCOS_PROXY_NAME)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter,
                    " Not found proxy contract address. resource: " + BCOSConstant.BCOS_PROXY_NAME);
        }

        if (!properties.containsKey(BCOSConstant.BCOS_GROUP_ID)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter,
                    " Not found groupId, resource: " + BCOSConstant.BCOS_PROXY_NAME);
        }

        if (!properties.containsKey(BCOSConstant.BCOS_CHAIN_ID)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter,
                    " Not found chainId, resource: " + BCOSConstant.BCOS_PROXY_NAME);
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

    public Map<String, String> getAbiMap() {
        return abiMap;
    }

    public void setAbiMap(Map<String, String> abiMap) {
        this.abiMap = abiMap;
    }

    public CommandHandlerDispatcher getCommandHandlerDispatcher() {
        return commandHandlerDispatcher;
    }

    public void setCommandHandlerDispatcher(CommandHandlerDispatcher commandHandlerDispatcher) {
        this.commandHandlerDispatcher = commandHandlerDispatcher;
    }
}
