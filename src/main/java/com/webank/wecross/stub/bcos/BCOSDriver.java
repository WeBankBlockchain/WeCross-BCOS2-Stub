package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import com.webank.wecross.stub.bcos.abi.ABICodecJsonWrapper;
import com.webank.wecross.stub.bcos.abi.ABIDefinition;
import com.webank.wecross.stub.bcos.abi.ABIDefinitionFactory;
import com.webank.wecross.stub.bcos.abi.ABIObject;
import com.webank.wecross.stub.bcos.abi.ABIObjectFactory;
import com.webank.wecross.stub.bcos.abi.ContractABIDefinition;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.common.RequestFactory;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.contract.RevertMessage;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.custom.CommandHandler;
import com.webank.wecross.stub.bcos.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import com.webank.wecross.stub.bcos.verify.MerkleValidation;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionDecoder;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.fisco.bcos.web3j.tuples.generated.Tuple5;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Driver implementation for BCOS */
public class BCOSDriver implements Driver {

    private static final Logger logger = LoggerFactory.getLogger(BCOSDriver.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private CommandHandlerDispatcher commandHandlerDispatcher;

    private AsyncCnsService asyncCnsService = new AsyncCnsService();
    private ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();

    public BCOSDriver() {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public AsyncCnsService getAsyncCnsService() {
        return asyncCnsService;
    }

    public void setAsyncCnsService(AsyncCnsService asyncCnsService) {
        this.asyncCnsService = asyncCnsService;
    }

    @Override
    public TransactionContext<TransactionRequest> decodeTransactionRequest(byte[] data) {
        try {
            TransactionParams transactionParams =
                    objectMapper.readValue(data, TransactionParams.class);

            if (logger.isTraceEnabled()) {
                logger.trace(" TransactionParams: {}", transactionParams);
            }

            Objects.requireNonNull(
                    transactionParams.getTransactionRequest(), "TransactionRequest is null");
            Objects.requireNonNull(transactionParams.getData(), "Data is null");
            Objects.requireNonNull(transactionParams.getTp_ype(), "type is null");

            TransactionRequest tr = transactionParams.getTransactionRequest();
            TransactionParams.TP_YPE tp_ype = transactionParams.getTp_ype();
            String abi = "";
            String encodeAbi = "";
            switch (tp_ype) {
                case SEND_TX_BY_PROXY:
                case CALL_BY_PROXY:
                    {
                        if (tp_ype == TransactionParams.TP_YPE.SEND_TX_BY_PROXY) {
                            ExtendedRawTransaction extendedRawTransaction =
                                    ExtendedTransactionDecoder.decode(transactionParams.getData());

                            if (extendedRawTransaction
                                    .getData()
                                    .startsWith(
                                            Numeric.cleanHexPrefix(
                                                    FunctionUtility
                                                            .ProxySendTransactionTXMethodId))) {
                                Tuple5<String, BigInteger, String, String, byte[]>
                                        sendTransactionProxyFunctionInput =
                                                FunctionUtility
                                                        .getSendTransactionProxyFunctionInput(
                                                                extendedRawTransaction.getData());
                                abi =
                                        Hex.toHexString(
                                                sendTransactionProxyFunctionInput.getValue5());
                            } else {
                                Tuple2<String, byte[]> sendTransactionProxyFunctionInput =
                                        FunctionUtility
                                                .getSendTransactionProxyWithoutTxIdFunctionInput(
                                                        extendedRawTransaction.getData());
                                abi =
                                        Hex.toHexString(
                                                sendTransactionProxyFunctionInput.getValue2());
                                abi = abi.substring(FunctionUtility.MethodIDLength);
                            }
                        } else {
                            if (transactionParams
                                    .getData()
                                    .startsWith(
                                            FunctionUtility.ProxyCallWithTransactionIdMethodId)) {
                                Tuple4<String, String, String, byte[]>
                                        constantCallProxyFunctionInput =
                                                FunctionUtility.getConstantCallProxyFunctionInput(
                                                        transactionParams.getData());
                                abi = Hex.toHexString(constantCallProxyFunctionInput.getValue4());
                            } else {
                                Tuple2<String, byte[]> sendTransactionProxyFunctionInput =
                                        FunctionUtility
                                                .getSendTransactionProxyWithoutTxIdFunctionInput(
                                                        transactionParams.getData());
                                abi =
                                        Hex.toHexString(
                                                sendTransactionProxyFunctionInput.getValue2());
                                abi = abi.substring(FunctionUtility.MethodIDLength);
                            }
                        }

                        List<ABIDefinition> abiDefinitions =
                                ABIDefinitionFactory.loadABI(transactionParams.getAbi())
                                        .getFunctions()
                                        .get(tr.getMethod());
                        if (Objects.isNull(abiDefinitions) || abiDefinitions.isEmpty()) {
                            throw new InvalidParameterException(
                                    " found no method in abi, method: " + tr.getMethod());
                        }

                        encodeAbi =
                                abiCodecJsonWrapper
                                        .encode(
                                                ABIObjectFactory.createInputObject(
                                                        abiDefinitions.get(0)),
                                                Objects.nonNull(tr.getArgs())
                                                        ? Arrays.asList(tr.getArgs())
                                                        : Arrays.asList())
                                        .encode();

                        break;
                    }
                case SEND_TX:
                case CALL:
                    {
                        if (tp_ype == TransactionParams.TP_YPE.SEND_TX) {
                            ExtendedRawTransaction extendedRawTransaction =
                                    ExtendedTransactionDecoder.decode(transactionParams.getData());
                            abi = extendedRawTransaction.getData();
                        } else {
                            abi = transactionParams.getData();
                        }

                        Function function =
                                FunctionUtility.newDefaultFunction(tr.getMethod(), tr.getArgs());

                        encodeAbi = FunctionEncoder.encode(function);
                        break;
                    }
                default:
                    {
                        throw new InvalidParameterException(" unknown tp type: " + tp_ype);
                    }
            }

            if (Numeric.cleanHexPrefix(encodeAbi).equals(Numeric.cleanHexPrefix(abi))) {
                return new TransactionContext<>(tr, null, null, null, null);
            }

            logger.warn(" abi not meet expectations, abi:{}, encodeAbi:{}", abi, encodeAbi);

        } catch (Exception e) {
            logger.error(" decodeTransactionRequest e: ", e);
        }

        return null;
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
            logger.error(" decodeBlockHeader Exception: ", e);
            return null;
        }
    }

    class CallAndSendTxResponseCallback implements Callback {

        public CallAndSendTxResponseCallback() {
            try {
                this.semaphore.acquire();
            } catch (InterruptedException e) {
                logger.error(" e: ", e);
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
            logger.error(" e: ", e);
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
            // checkRequest(request);
            checkProperties(properties);

            String contractAddress = properties.get(BCOSConstant.BCOS_PROXY_NAME);
            Path path = request.getPath();
            String name = path.getResource();

            // query abi
            asyncCnsService.queryABI(
                    name,
                    this,
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
                                        "resource:" + name + " not exist");
                            }

                            // encode
                            String[] args = request.getData().getArgs();
                            String method = request.getData().getMethod();
                            ContractABIDefinition contractABIDefinition =
                                    ABIDefinitionFactory.loadABI(abi);

                            List<ABIDefinition> functions =
                                    contractABIDefinition.getFunctions().get(method);
                            if (Objects.isNull(functions) || functions.isEmpty()) {
                                throw new BCOSStubException(
                                        BCOSStatusCode.MethodNotExist, "method not found in abi");
                            }

                            // Overloading is not supported ???
                            ABIObject inputObj =
                                    ABIObjectFactory.createInputObject(functions.get(0));

                            String encodedArgs = "";
                            if (!Objects.isNull(args)) {
                                ABIObject encodedObj =
                                        abiCodecJsonWrapper.encode(inputObj, Arrays.asList(args));
                                encodedArgs = encodedObj.encode();
                            }

                            String transactionID =
                                    (String)
                                            request.getData()
                                                    .getOptions()
                                                    .get(BCOSConstant.TRANSACTION_ID);

                            Function function = null;
                            if (Objects.isNull(transactionID) || transactionID.isEmpty()) {
                                function =
                                        FunctionUtility.newConstantCallProxyFunction(
                                                path.getResource(),
                                                functions.get(0).getMethodSignatureAsString(),
                                                encodedArgs);
                            } else {
                                function =
                                        FunctionUtility.newConstantCallProxyFunction(
                                                transactionID,
                                                path.toString(),
                                                functions.get(0).getMethodSignatureAsString(),
                                                encodedArgs);
                            }

                            // BCOSAccount to get credentials to sign the transaction
                            String from = BCOSConstant.DEFAULT_ADDRESS;
                            if (Objects.nonNull(request.getAccount())) {
                                BCOSAccount bcosAccount = (BCOSAccount) request.getAccount();
                                Credentials credentials = bcosAccount.getCredentials();
                                from = credentials.getAddress();
                            }

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
                                            TransactionParams.TP_YPE.CALL_BY_PROXY);
                            transaction.setFrom(from);
                            transaction.setTo(contractAddress);
                            transaction.setAbi(abi);

                            Request req =
                                    RequestFactory.requestBuilder(
                                            BCOSRequestType.CALL,
                                            objectMapper.writeValueAsBytes(transaction));

                            // decodeTransactionRequest(req.getData());

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

                                                ABIObject outputObj =
                                                        ABIObjectFactory.createOutputObject(
                                                                functions.get(0));

                                                // decode outputs
                                                String output =
                                                        callOutput.getOutput().substring(130);
                                                transactionResponse.setResult(
                                                        abiCodecJsonWrapper
                                                                .decode(outputObj, output)
                                                                .toArray(new String[0]));
                                            } else {
                                                transactionResponse.setErrorCode(
                                                        BCOSStatusCode.CallNotSuccessStatus);

                                                Tuple2<Boolean, String> booleanStringTuple2 =
                                                        RevertMessage.tryParserRevertMessage(
                                                                callOutput.getStatus(),
                                                                callOutput.getOutput());
                                                if (booleanStringTuple2.getValue1()) {
                                                    transactionResponse.setErrorMessage(
                                                            booleanStringTuple2.getValue2());
                                                } else {
                                                    transactionResponse.setErrorMessage(
                                                            StatusCode.getStatusMessage(
                                                                    callOutput.getStatus()));
                                                }
                                            }

                                            callback.onTransactionResponse(
                                                    null, transactionResponse);

                                        } catch (BCOSStubException e) {
                                            logger.warn(" e: ", e);
                                            callback.onTransactionResponse(
                                                    new TransactionException(
                                                            e.getErrorCode(), e.getMessage()),
                                                    null);
                                        } catch (Exception e) {
                                            logger.warn(" e: ", e);
                                            callback.onTransactionResponse(
                                                    new TransactionException(
                                                            BCOSStatusCode.UnclassifiedError,
                                                            " errorMessage: " + e.getMessage()),
                                                    null);
                                        }
                                    });

                        } catch (BCOSStubException bse) {
                            logger.warn(" e: ", bse);
                            callback.onTransactionResponse(
                                    new TransactionException(bse.getErrorCode(), bse.getMessage()),
                                    null);
                        } catch (Exception e) {
                            logger.warn(" e: ", e);
                            callback.onTransactionResponse(
                                    new TransactionException(
                                            BCOSStatusCode.UnclassifiedError, e.getMessage()),
                                    null);
                        }
                    });

        } catch (BCOSStubException e) {
            logger.warn(" e: ", e);
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
                    FunctionUtility.newDefaultFunction(
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
                            TransactionParams.TP_YPE.CALL);

            transaction.setFrom(credentials.getAddress());
            transaction.setTo(contractAddress);
            Request req =
                    RequestFactory.requestBuilder(
                            BCOSRequestType.CALL, objectMapper.writeValueAsBytes(transaction));

            // decodeTransactionRequest(req.getData());

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
                                        FunctionUtility.decodeDefaultOutput(
                                                callOutput.getOutput()));
                            } else {
                                transactionResponse.setErrorCode(
                                        BCOSStatusCode.CallNotSuccessStatus);

                                Tuple2<Boolean, String> booleanStringTuple2 =
                                        RevertMessage.tryParserRevertMessage(
                                                callOutput.getStatus(), callOutput.getOutput());
                                if (booleanStringTuple2.getValue1()) {
                                    transactionResponse.setErrorMessage(
                                            booleanStringTuple2.getValue2());
                                } else {
                                    transactionResponse.setErrorMessage(
                                            StatusCode.getStatusMessage(callOutput.getStatus()));
                                }
                            }

                            callback.onTransactionResponse(null, transactionResponse);

                        } catch (BCOSStubException e) {
                            logger.warn(" e: ", e);
                            callback.onTransactionResponse(
                                    new TransactionException(e.getErrorCode(), e.getMessage()),
                                    null);
                        } catch (Exception e) {
                            logger.warn(" e: ", e);
                            callback.onTransactionResponse(
                                    new TransactionException(
                                            BCOSStatusCode.UnclassifiedError,
                                            " errorMessage: " + e.getMessage()),
                                    null);
                        }
                    });
        } catch (BCOSStubException e) {
            logger.warn(" e: ", e);
            callback.onTransactionResponse(
                    new TransactionException(e.getErrorCode(), e.getMessage()), null);
        } catch (Exception e) {
            logger.warn(" e: ", e);
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
            logger.error(" e: ", e);
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
                                        FunctionUtility.newDefaultFunction(
                                                request.getData().getMethod(),
                                                request.getData().getArgs());

                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "asyncSendTransaction contractAddress: {}, blockNumber: {}, method: {}, args: {}",
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
                                        new TransactionParams(
                                                request.getData(),
                                                signTx,
                                                TransactionParams.TP_YPE.SEND_TX);

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

                                // decodeTransactionRequest(req.getData());

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
                                                                                                    .decodeDefaultOutput(
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
                                                                            logger.warn(" e: ", e);
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
                                                    if (StatusCode.RevertInstruction.equals(
                                                            receipt.getStatus())) {
                                                        Tuple2<Boolean, String>
                                                                booleanStringTuple2 =
                                                                        RevertMessage
                                                                                .tryParserRevertMessage(
                                                                                        receipt
                                                                                                .getStatus(),
                                                                                        receipt
                                                                                                .getOutput());
                                                        if (booleanStringTuple2.getValue1()) {
                                                            transactionResponse.setErrorMessage(
                                                                    booleanStringTuple2
                                                                            .getValue2());
                                                        } else {
                                                            // return revert message
                                                            transactionResponse.setErrorMessage(
                                                                    receipt.getMessage());
                                                        }
                                                    } else {
                                                        transactionResponse.setErrorMessage(
                                                                StatusCode.getStatusMessage(
                                                                        receipt.getStatus()));
                                                    }
                                                    callback.onTransactionResponse(
                                                            null, transactionResponse);
                                                }
                                            } catch (BCOSStubException e) {
                                                logger.warn(" e: ", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                e.getErrorCode(), e.getMessage()),
                                                        null);
                                            } catch (Exception e) {
                                                logger.warn(" e: ", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                BCOSStatusCode.UnclassifiedError,
                                                                " errorMessage: " + e.getMessage()),
                                                        null);
                                            }
                                        });
                            });

        } catch (BCOSStubException e) {
            logger.warn(" e: ", e);
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

                                Path path = request.getPath();
                                String name = path.getResource();

                                // query abi
                                asyncCnsService.queryABI(
                                        name,
                                        this,
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
                                                            "resource:" + name + " not exist");
                                                }

                                                // encode
                                                String[] args = request.getData().getArgs();
                                                String method = request.getData().getMethod();
                                                ContractABIDefinition contractABIDefinition =
                                                        ABIDefinitionFactory.loadABI(abi);

                                                List<ABIDefinition> functions =
                                                        contractABIDefinition
                                                                .getFunctions()
                                                                .get(method);
                                                if (Objects.isNull(functions)
                                                        || functions.isEmpty()) {
                                                    throw new BCOSStubException(
                                                            BCOSStatusCode.MethodNotExist,
                                                            "method not found in abi");
                                                }

                                                ABIObject inputObj =
                                                        ABIObjectFactory.createInputObject(
                                                                functions.get(0));

                                                String encodedArgs = "";
                                                if (!Objects.isNull(args)) {
                                                    ABIObject encodedObj =
                                                            abiCodecJsonWrapper.encode(
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

                                                Function function = null;
                                                if (Objects.isNull(transactionID)
                                                        || transactionID.isEmpty()) {
                                                    function =
                                                            FunctionUtility
                                                                    .newSendTransactionProxyFunction(
                                                                            path.getResource(),
                                                                            functions
                                                                                    .get(0)
                                                                                    .getMethodSignatureAsString(),
                                                                            encodedArgs);
                                                } else {
                                                    function =
                                                            FunctionUtility
                                                                    .newSendTransactionProxyFunction(
                                                                            transactionID,
                                                                            seq,
                                                                            path.toString(),
                                                                            functions
                                                                                    .get(0)
                                                                                    .getMethodSignatureAsString(),
                                                                            encodedArgs);
                                                }

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
                                                                request.getData(),
                                                                signTx,
                                                                TransactionParams.TP_YPE
                                                                        .SEND_TX_BY_PROXY);

                                                transaction.setAbi(abi);
                                                Request req =
                                                        RequestFactory.requestBuilder(
                                                                BCOSRequestType.SEND_TRANSACTION,
                                                                objectMapper.writeValueAsBytes(
                                                                        transaction));

                                                // decodeTransactionRequest(req.getData());

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
                                                                if (logger.isDebugEnabled()) {
                                                                    logger.debug(
                                                                            "TransactionReceipt: {}",
                                                                            receipt);
                                                                }

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
                                                                                                            ABIObjectFactory
                                                                                                                    .createOutputObject(
                                                                                                                            functions
                                                                                                                                    .get(
                                                                                                                                            0));
                                                                                            transactionResponse
                                                                                                    .setResult(
                                                                                                            abiCodecJsonWrapper
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
                                                                                                            " e: ",
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
                                                                    if (StatusCode.RevertInstruction
                                                                            .equals(
                                                                                    receipt
                                                                                            .getStatus())) {
                                                                        Tuple2<Boolean, String>
                                                                                booleanStringTuple2 =
                                                                                        RevertMessage
                                                                                                .tryParserRevertMessage(
                                                                                                        receipt
                                                                                                                .getStatus(),
                                                                                                        receipt
                                                                                                                .getOutput());
                                                                        if (booleanStringTuple2
                                                                                .getValue1()) {
                                                                            transactionResponse
                                                                                    .setErrorMessage(
                                                                                            booleanStringTuple2
                                                                                                    .getValue2());
                                                                        } else {
                                                                            // return revert message
                                                                            transactionResponse
                                                                                    .setErrorMessage(
                                                                                            receipt
                                                                                                    .getMessage());
                                                                        }
                                                                    } else {
                                                                        transactionResponse
                                                                                .setErrorMessage(
                                                                                        StatusCode
                                                                                                .getStatusMessage(
                                                                                                        receipt
                                                                                                                .getStatus()));
                                                                    }

                                                                    callback.onTransactionResponse(
                                                                            null,
                                                                            transactionResponse);
                                                                }
                                                            } catch (BCOSStubException e) {
                                                                logger.warn(" e: ", e);
                                                                callback.onTransactionResponse(
                                                                        new TransactionException(
                                                                                e.getErrorCode(),
                                                                                e.getMessage()),
                                                                        null);
                                                            } catch (Exception e) {
                                                                logger.warn(" e: ", e);
                                                                callback.onTransactionResponse(
                                                                        new TransactionException(
                                                                                BCOSStatusCode
                                                                                        .UnclassifiedError,
                                                                                e.getMessage()),
                                                                        null);
                                                            }
                                                        });

                                            } catch (BCOSStubException e) {
                                                logger.warn(" e: ", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                e.getErrorCode(), e.getMessage()),
                                                        null);
                                            } catch (Exception e) {
                                                logger.warn(" e: ", e);
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                BCOSStatusCode.UnclassifiedError,
                                                                e.getMessage()),
                                                        null);
                                            }
                                        });
                            });

        } catch (BCOSStubException e) {
            logger.warn(" e: ", e);
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
            Path path,
            String transactionHash,
            long blockNumber,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            GetVerifiedTransactionCallback callback) {

        final Long[] finalBlockNumber = {blockNumber};
        // get transaction proof
        asyncRequestTransactionProof(
                transactionHash,
                connection,
                (exception, proof) -> {
                    if (Objects.nonNull(exception)) {
                        logger.warn("transactionHash: {} exception: ", transactionHash, exception);
                        callback.onResponse(exception, null);
                        return;
                    }

                    try {
                        TransactionReceipt receipt =
                                proof.getReceiptAndProof().getTransactionReceipt();
                        if (finalBlockNumber[0] != receipt.getBlockNumber().longValue()) {
                            logger.warn(
                                    " invalid blockNumber, blockNumber: {}, receipt blockNumber: {}",
                                    finalBlockNumber[0],
                                    receipt.getBlockNumber());
                            finalBlockNumber[0] = receipt.getBlockNumber().longValue();
                        }

                        MerkleValidation merkleValidation = new MerkleValidation();
                        merkleValidation.verifyTransactionProof(
                                finalBlockNumber[0],
                                transactionHash,
                                blockHeaderManager,
                                proof,
                                verifyException -> {
                                    if (Objects.nonNull(verifyException)) {
                                        callback.onResponse(verifyException, null);
                                        return;
                                    }

                                    String proxyInput =
                                            proof.getReceiptAndProof()
                                                    .getTransactionReceipt()
                                                    .getInput();

                                    String proxyOutput =
                                            proof.getReceiptAndProof()
                                                    .getTransactionReceipt()
                                                    .getOutput();

                                    String methodId;
                                    String input;

                                    if (proxyInput.startsWith(
                                            FunctionUtility.ProxySendTXMethodId)) {
                                        Tuple2<String, byte[]> proxyResult =
                                                FunctionUtility
                                                        .getSendTransactionProxyWithoutTxIdFunctionInput(
                                                                proxyInput);
                                        String resource = proxyResult.getValue1();
                                        input = Numeric.toHexString(proxyResult.getValue2());
                                        methodId =
                                                input.substring(
                                                        0,
                                                        FunctionUtility
                                                                .MethodIDWithHexPrefixLength);
                                        input =
                                                input.substring(
                                                        FunctionUtility
                                                                .MethodIDWithHexPrefixLength);

                                        if (logger.isDebugEnabled()) {
                                            logger.debug(
                                                    "  resource: {}, methodId: {}",
                                                    resource,
                                                    methodId);
                                        }

                                        if (!path.getResource().equals(resource)) {
                                            callback.onResponse(
                                                    new Exception(
                                                            " Resource does not matches, expected: "
                                                                    + path.getResource()
                                                                    + " ,actual: "
                                                                    + resource),
                                                    null);
                                            return;
                                        }

                                    } else {
                                        Tuple5<String, BigInteger, String, String, byte[]>
                                                proxyInputResult =
                                                        FunctionUtility
                                                                .getSendTransactionProxyFunctionInput(
                                                                        proxyInput);

                                        String transactionID = proxyInputResult.getValue1();
                                        BigInteger seq = proxyInputResult.getValue2();
                                        String strPath = proxyInputResult.getValue3();
                                        String methodSignature = proxyInputResult.getValue4();

                                        input = Numeric.toHexString(proxyInputResult.getValue5());
                                        methodId = FunctionEncoder.buildMethodId(methodSignature);

                                        if (logger.isDebugEnabled()) {
                                            logger.debug(
                                                    "transactionID: {}, seq: {}, path: {}, method: {}, methodId: {}",
                                                    transactionID,
                                                    seq,
                                                    strPath,
                                                    methodSignature,
                                                    methodId);
                                        }

                                        Path proxyPath;
                                        try {
                                            proxyPath = Path.decode(strPath);
                                            if (!path.equals(proxyPath)) {
                                                callback.onResponse(
                                                        new Exception(
                                                                " Path does not matches, expected: "
                                                                        + path.toString()
                                                                        + " ,actual: "
                                                                        + strPath),
                                                        null);
                                                return;
                                            }
                                        } catch (Exception e) {
                                            logger.error(" e: ", e);
                                            callback.onResponse(
                                                    new Exception(
                                                            " invalid path format, path: "
                                                                    + strPath
                                                                    + " ,e: "
                                                                    + e.getMessage()),
                                                    null);
                                            return;
                                        }
                                    }

                                    // query ABI
                                    String finalMethodId = methodId;
                                    String finalInput = input;
                                    asyncCnsService.queryABI(
                                            path.getResource(),
                                            this,
                                            connection,
                                            (queryABIException, abi) -> {
                                                if (Objects.nonNull(queryABIException)) {
                                                    logger.error(" e: ", queryABIException);
                                                    callback.onResponse(
                                                            new TransactionException(
                                                                    BCOSStatusCode.QueryAbiFailed,
                                                                    queryABIException.getMessage()),
                                                            null);
                                                    return;
                                                }

                                                ABIDefinition function =
                                                        ABIDefinitionFactory.loadABI(abi)
                                                                .getMethodIDToFunctions()
                                                                .get(finalMethodId);

                                                if (Objects.isNull(function)) {
                                                    // logger.error(" e: ", queryABIException);
                                                    callback.onResponse(
                                                            new TransactionException(
                                                                    BCOSStatusCode.MethodNotExist,
                                                                    "methodId not found in abi, methodId: "
                                                                            + finalMethodId),
                                                            null);
                                                    return;
                                                }

                                                ABIObject inputObject =
                                                        ABIObjectFactory.createInputObject(
                                                                function);

                                                List<String> inputParams =
                                                        abiCodecJsonWrapper.decode(
                                                                inputObject, finalInput);

                                                TransactionRequest transactionRequest =
                                                        new TransactionRequest();
                                                transactionRequest.setMethod(function.getName());
                                                /** decode input args from input */
                                                transactionRequest.setArgs(
                                                        inputParams.toArray(new String[0]));

                                                TransactionResponse transactionResponse =
                                                        new TransactionResponse();
                                                transactionResponse.setHash(transactionHash);
                                                transactionResponse.setBlockNumber(
                                                        receipt.getBlockNumber().longValue());

                                                /** set error code and error message info */
                                                transactionResponse.setErrorMessage(
                                                        StatusCode.getStatusMessage(
                                                                receipt.getStatus()));

                                                if (StatusCode.Success.equals(
                                                        receipt.getStatus())) {
                                                    ABIObject outputObject =
                                                            ABIObjectFactory.createOutputObject(
                                                                    function);
                                                    List<String> outputParams =
                                                            abiCodecJsonWrapper.decode(
                                                                    outputObject,
                                                                    proxyOutput.substring(130));
                                                    /** decode output from output */
                                                    transactionResponse.setResult(
                                                            outputParams.toArray(new String[0]));
                                                }

                                                BigInteger statusCode =
                                                        new BigInteger(
                                                                Numeric.cleanHexPrefix(
                                                                        receipt.getStatus()),
                                                                16);
                                                transactionResponse.setErrorCode(
                                                        statusCode.intValue());

                                                VerifiedTransaction verifiedTransaction =
                                                        new VerifiedTransaction(
                                                                finalBlockNumber[0],
                                                                transactionHash,
                                                                path,
                                                                receipt.getTo(),
                                                                transactionRequest,
                                                                transactionResponse);

                                                logger.trace(
                                                        " VerifiedTransaction: {}",
                                                        verifiedTransaction);
                                                callback.onResponse(null, verifiedTransaction);
                                            });
                                });
                    } catch (Exception e) {
                        logger.warn("transactionHash: {} exception: ", transactionHash, e);
                        callback.onResponse(e, null);
                    }
                });
    }

    private interface RequestTransactionProofCallback {
        void onResponse(BCOSStubException e, TransactionProof proof);
    }

    /**
     * @param transactionHash
     * @param connection
     */
    private void asyncRequestTransactionProof(
            String transactionHash,
            Connection connection,
            RequestTransactionProofCallback callback) {

        Request request =
                RequestFactory.requestBuilder(
                        BCOSRequestType.GET_TRANSACTION_PROOF, transactionHash);
        connection.asyncSend(
                request,
                response -> {
                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Request proof, request: {}, response: {}", request, response);
                        }

                        if (response.getErrorCode() != BCOSStatusCode.Success) {
                            callback.onResponse(
                                    new BCOSStubException(
                                            response.getErrorCode(), response.getErrorMessage()),
                                    null);
                            return;
                        }

                        TransactionProof transactionProof =
                                objectMapper.readValue(response.getData(), TransactionProof.class);
                        logger.debug(
                                " transactionHash: {}, transactionProof: {}",
                                transactionHash,
                                transactionProof);

                        callback.onResponse(null, transactionProof);
                    } catch (Exception e) {
                        callback.onResponse(
                                new BCOSStubException(
                                        BCOSStatusCode.UnclassifiedError,
                                        "Request transaction proof failed"),
                                null);
                    }
                });
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
                                        request,
                                        account,
                                        path,
                                        new ResourceInfo(),
                                        blockHeaderManager);
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

        if (Objects.isNull(request.getBlockHeaderManager())) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter, "BlockHeaderManager is null");
        }

        if (Objects.isNull(request.getAccount())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "Account is null");
        }

        /*
        if (Objects.isNull(request.getResourceInfo())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "ResourceInfo is null");
        }*/

        if (Objects.isNull(request.getData())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "Data is null");
        }

        if (Objects.isNull(request.getData().getMethod())
                || "".equals(request.getData().getMethod())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "Method is null");
        }
    }

    public CommandHandlerDispatcher getCommandHandlerDispatcher() {
        return commandHandlerDispatcher;
    }

    public void setCommandHandlerDispatcher(CommandHandlerDispatcher commandHandlerDispatcher) {
        this.commandHandlerDispatcher = commandHandlerDispatcher;
    }
}
