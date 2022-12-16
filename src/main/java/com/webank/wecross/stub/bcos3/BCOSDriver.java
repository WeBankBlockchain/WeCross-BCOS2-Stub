package com.webank.wecross.stub.bcos3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.StubConstant;
import com.webank.wecross.stub.Transaction;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.bcos3.account.BCOSAccount;
import com.webank.wecross.stub.bcos3.common.BCOSBlockHeader;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import com.webank.wecross.stub.bcos3.common.BCOSRequestType;
import com.webank.wecross.stub.bcos3.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos3.common.BCOSStubException;
import com.webank.wecross.stub.bcos3.common.ObjectMapperFactory;
import com.webank.wecross.stub.bcos3.contract.BlockUtility;
import com.webank.wecross.stub.bcos3.contract.FunctionUtility;
import com.webank.wecross.stub.bcos3.contract.RevertMessage;
import com.webank.wecross.stub.bcos3.custom.CommandHandler;
import com.webank.wecross.stub.bcos3.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.bcos3.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos3.protocol.response.TransactionProof;
import com.webank.wecross.stub.bcos3.uaproof.Signer;
import com.webank.wecross.stub.bcos3.verify.BlockHeaderValidation;
import com.webank.wecross.stub.bcos3.verify.MerkleValidation;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.fisco.bcos.sdk.jni.utilities.tx.TransactionBuilderJniObj;
import org.fisco.bcos.sdk.jni.utilities.tx.TxPair;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.response.Call;
import org.fisco.bcos.sdk.v3.codec.FunctionEncoderInterface;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple4;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple6;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIObject;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIObjectFactory;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractCodecJsonWrapper;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.v3.utils.Hex;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Driver implementation for BCOS */
public class BCOSDriver implements Driver {

    private static final Logger logger = LoggerFactory.getLogger(BCOSDriver.class);

    private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private AsyncBfsService asyncBfsService;
    private CommandHandlerDispatcher commandHandlerDispatcher;

    private final ContractCodecJsonWrapper contractCodecJsonWrapper;
    private final CryptoSuite cryptoSuite;
    private final boolean isWasm;
    private final ABIDefinitionFactory abiDefinitionFactory;
    private final Signer signer;
    private final FunctionEncoderInterface functionEncoder;

    public BCOSDriver(CryptoSuite cryptoSuite, boolean isWasm) {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.contractCodecJsonWrapper = new ContractCodecJsonWrapper();
        this.cryptoSuite = cryptoSuite;
        this.isWasm = isWasm;
        this.abiDefinitionFactory = new ABIDefinitionFactory(cryptoSuite);
        this.signer = Signer.newSigner(cryptoSuite.getCryptoTypeConfig());
        this.functionEncoder =
                (isWasm
                        ? new org.fisco.bcos.sdk.v3.codec.scale.FunctionEncoder(cryptoSuite)
                        : new org.fisco.bcos.sdk.v3.codec.abi.FunctionEncoder(cryptoSuite));
    }

    @Override
    public ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request) {

        int requestType = request.getType();
        /** check if transaction request */
        if ((requestType != BCOSRequestType.CALL)
                && (requestType != BCOSRequestType.SEND_TRANSACTION)) {
            return new ImmutablePair<>(false, null);
        }

        try {
            TransactionParams transactionParams =
                    objectMapper.readValue(request.getData(), TransactionParams.class);

            if (logger.isTraceEnabled()) {
                logger.trace(" TransactionParams: {}", transactionParams);
            }

            Objects.requireNonNull(
                    transactionParams.getTransactionRequest(), "TransactionRequest is null");
            Objects.requireNonNull(transactionParams.getData(), "Data is null");
            Objects.requireNonNull(transactionParams.getSubType(), "type is null");

            TransactionRequest transactionRequest = transactionParams.getTransactionRequest();
            TransactionParams.SUB_TYPE subType = transactionParams.getSubType();
            String abi = "";
            String encodeAbi = "";
            switch (subType) {
                case SEND_TX_BY_PROXY:
                case CALL_BY_PROXY:
                    {
                        if (subType == TransactionParams.SUB_TYPE.SEND_TX_BY_PROXY) {
                            JsonTransactionResponse jsonTransactionResponse =
                                    JsonTransactionResponse.readFromHexString(
                                            transactionParams.getData());
                            String input =
                                    Numeric.cleanHexPrefix(jsonTransactionResponse.getInput());
                            if (input.startsWith(
                                    Hex.toHexString(
                                            functionEncoder.buildMethodId(
                                                    FunctionUtility
                                                            .ProxySendTransactionTXMethod)))) {
                                Tuple6<String, String, BigInteger, String, String, byte[]>
                                        sendTransactionProxyFunctionInput =
                                                FunctionUtility
                                                        .getSendTransactionProxyFunctionInput(
                                                                input);
                                abi =
                                        Hex.toHexString(
                                                sendTransactionProxyFunctionInput.getValue6());
                            } else {
                                Tuple3<String, String, byte[]> sendTransactionProxyFunctionInput =
                                        FunctionUtility
                                                .getSendTransactionProxyWithoutTxIdFunctionInput(
                                                        input);
                                abi =
                                        Hex.toHexString(
                                                sendTransactionProxyFunctionInput.getValue3());
                                abi = abi.substring(FunctionUtility.MethodIDLength);
                            }
                        } else {
                            if (transactionParams
                                    .getData()
                                    .startsWith(
                                            Hex.toHexString(
                                                    functionEncoder.buildMethodId(
                                                            FunctionUtility
                                                                    .ProxyCallWithTransactionIdMethod)))) {
                                Tuple4<String, String, String, byte[]>
                                        constantCallProxyFunctionInput =
                                                FunctionUtility.getConstantCallProxyFunctionInput(
                                                        transactionParams.getData());
                                abi = Hex.toHexString(constantCallProxyFunctionInput.getValue4());
                            } else {
                                Tuple2<String, byte[]> sendTransactionProxyFunctionInput =
                                        FunctionUtility.getConstantCallFunctionInput(
                                                transactionParams.getData());
                                abi =
                                        Hex.toHexString(
                                                sendTransactionProxyFunctionInput.getValue2());
                                abi = abi.substring(FunctionUtility.MethodIDLength);
                            }
                        }

                        List<ABIDefinition> abiDefinitions =
                                abiDefinitionFactory
                                        .loadABI(transactionParams.getAbi())
                                        .getFunctions()
                                        .get(transactionRequest.getMethod());
                        if (Objects.isNull(abiDefinitions) || abiDefinitions.isEmpty()) {
                            throw new InvalidParameterException(
                                    " found no method in abi, method: "
                                            + transactionRequest.getMethod());
                        }

                        encodeAbi =
                                Hex.toHexString(
                                        contractCodecJsonWrapper
                                                .encode(
                                                        ABIObjectFactory.createInputObject(
                                                                abiDefinitions.get(0)),
                                                        Objects.nonNull(
                                                                        transactionRequest
                                                                                .getArgs())
                                                                ? Arrays.asList(
                                                                        transactionRequest
                                                                                .getArgs())
                                                                : Arrays.asList())
                                                .encode(isWasm));

                        break;
                    }
                case SEND_TX:
                case CALL:
                    {
                        if (subType == TransactionParams.SUB_TYPE.SEND_TX) {
                            JsonTransactionResponse jsonTransactionResponse =
                                    JsonTransactionResponse.readFromHexString(
                                            transactionParams.getData());
                            abi = Numeric.cleanHexPrefix(jsonTransactionResponse.getInput());
                        } else {
                            abi = transactionParams.getData();
                        }

                        Function function =
                                FunctionUtility.newDefaultFunction(
                                        transactionRequest.getMethod(),
                                        transactionRequest.getArgs());

                        encodeAbi = Hex.toHexString(functionEncoder.encode(function));
                        break;
                    }
                default:
                    {
                        // not call/sendTransaction
                        return new ImmutablePair<>(true, null);
                    }
            }

            if (Numeric.cleanHexPrefix(encodeAbi).equals(Numeric.cleanHexPrefix(abi))) {
                return new ImmutablePair<>(true, transactionRequest);
            }

            logger.warn(" abi not meet expectations, abi:{}, encodeAbi:{}", abi, encodeAbi);
            return new ImmutablePair<>(true, null);

        } catch (Exception e) {
            logger.error(" decodeTransactionRequest e: ", e);
            return new ImmutablePair<>(true, null);
        }
    }

    @Override
    public List<ResourceInfo> getResources(Connection connection) {
        if (connection instanceof BCOSConnection) {
            return ((BCOSConnection) connection).getResources();
        }

        logger.error("Not BCOS connection, connection name: {}", connection.getClass().getName());
        return new ArrayList<>();
    }

    @Override
    public void asyncCall(
            TransactionContext context,
            TransactionRequest request,
            boolean byProxy,
            Connection connection,
            Callback callback) {
        asyncCallByProxy(context, request, connection, callback);
    }

    @Override
    public void asyncSendTransaction(
            TransactionContext context,
            TransactionRequest request,
            boolean byProxy,
            Connection connection,
            Callback callback) {
        asyncSendTransactionByProxy(context, request, connection, callback);
    }

    private void asyncCallByProxy(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback) {

        TransactionResponse transactionResponse = new TransactionResponse();

        try {
            Map<String, String> properties = connection.getProperties();

            // input validationx
            checkProperties(properties);

            String contractAddress = properties.get(BCOSConstant.BCOS_PROXY_NAME);
            Path path = context.getPath();
            String name = path.getResource();

            // query abi
            asyncBfsService.queryABI(
                    name,
                    this,
                    connection,
                    (queryABIException, abi) -> {
                        try {
                            if (Objects.nonNull(queryABIException)) {
                                throw new BCOSStubException(
                                        BCOSStatusCode.ABINotExist, queryABIException.getMessage());
                            }

                            if (abi == null) {
                                throw new BCOSStubException(
                                        BCOSStatusCode.ABINotExist,
                                        "resource:" + name + " not exist");
                            }

                            // encode
                            String[] args = request.getArgs();
                            String method = request.getMethod();
                            ContractABIDefinition contractABIDefinition =
                                    abiDefinitionFactory.loadABI(abi);

                            List<ABIDefinition> functions =
                                    contractABIDefinition.getFunctions().get(method);
                            if (Objects.isNull(functions) || functions.isEmpty()) {
                                throw new BCOSStubException(
                                        BCOSStatusCode.MethodNotExist,
                                        "Method not found in abi, method: " + method);
                            }

                            ABIObject inputObj =
                                    ABIObjectFactory.createInputObject(functions.get(0));

                            byte[] encodedArgs = null;
                            if (!Objects.isNull(args)) {
                                ABIObject encodedObj =
                                        contractCodecJsonWrapper.encode(
                                                inputObj, Arrays.asList(args));
                                encodedArgs = encodedObj.encode(isWasm);
                            }

                            String transactionID =
                                    (String)
                                            request.getOptions()
                                                    .get(StubConstant.XA_TRANSACTION_ID);

                            Function function;
                            if (Objects.isNull(transactionID)
                                    || transactionID.isEmpty()
                                    || "0".equals(transactionID)) {
                                function =
                                        FunctionUtility.newConstantCallProxyFunction(
                                                functionEncoder,
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
                            if (Objects.nonNull(context.getAccount())) {
                                BCOSAccount bcosAccount = (BCOSAccount) context.getAccount();
                                CryptoKeyPair credentials = bcosAccount.getCredentials();
                                from = credentials.getAddress();
                            }

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        " name:{}, address: {}, method: {}, args: {}",
                                        BCOSConstant.BCOS_PROXY_NAME,
                                        contractAddress,
                                        request.getMethod(),
                                        request.getArgs());
                            }

                            TransactionParams transaction =
                                    new TransactionParams(
                                            request,
                                            Hex.toHexString(functionEncoder.encode(function)),
                                            TransactionParams.SUB_TYPE.CALL_BY_PROXY);
                            transaction.setFrom(from);
                            transaction.setTo(contractAddress);
                            transaction.setAbi(abi);

                            Request req =
                                    Request.newRequest(
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
                                                        callOutput.getBlockNumber());
                                            }

                                            if (Objects.equals(
                                                    TransactionReceiptStatus.Success.getCode(),
                                                    callOutput.getStatus())) {
                                                transactionResponse.setErrorCode(
                                                        BCOSStatusCode.Success);
                                                transactionResponse.setMessage(
                                                        BCOSStatusCode.getStatusMessage(
                                                                BCOSStatusCode.Success));

                                                ABIObject outputObj =
                                                        ABIObjectFactory.createOutputObject(
                                                                functions.get(0));

                                                // decode 130
                                                String output =
                                                        callOutput.getOutput().substring(130);
                                                transactionResponse.setResult(
                                                        contractCodecJsonWrapper
                                                                .decode(
                                                                        outputObj,
                                                                        Hex.decode(output),
                                                                        isWasm)
                                                                .toArray(new String[0]));
                                            } else {
                                                transactionResponse.setErrorCode(
                                                        BCOSStatusCode.CallNotSuccessStatus);

                                                Tuple2<Boolean, String> booleanStringTuple2 =
                                                        RevertMessage.tryParserRevertMessage(
                                                                callOutput.getStatus(),
                                                                callOutput.getOutput());
                                                if (booleanStringTuple2.getValue1()) {
                                                    transactionResponse.setMessage(
                                                            booleanStringTuple2.getValue2());
                                                } else {
                                                    transactionResponse.setMessage(
                                                            TransactionReceiptStatus
                                                                    .getStatusMessage(
                                                                            callOutput.getStatus(),
                                                                            "Unknown error")
                                                                    .getMessage());
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
                                                            e.getMessage()),
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

    private void asyncSendTransactionByProxy(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback) {
        try {
            Map<String, String> properties = connection.getProperties();

            // input validation
            checkTransactionRequest(context, request);
            checkProperties(properties);

            // contractAddress
            String contractAddress = properties.get(BCOSConstant.BCOS_PROXY_NAME);
            // groupId
            String groupId = properties.get(BCOSConstant.BCOS_GROUP_ID);
            // chainId
            String chainId = properties.get(BCOSConstant.BCOS_CHAIN_ID);

            context.getBlockManager()
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
                                BCOSAccount bcosAccount = (BCOSAccount) context.getAccount();
                                CryptoKeyPair credentials = bcosAccount.getCredentials();

                                Path path = context.getPath();
                                String name = path.getResource();

                                // query abi
                                queryAbiAndSendTx(
                                        context,
                                        request,
                                        connection,
                                        callback,
                                        contractAddress,
                                        groupId,
                                        chainId,
                                        blockNumber,
                                        credentials,
                                        path,
                                        name);
                            });

        } catch (BCOSStubException e) {
            logger.warn(" e: ", e);
            callback.onTransactionResponse(
                    new TransactionException(e.getErrorCode(), e.getMessage()), null);
        }
    }

    private void queryAbiAndSendTx(
            TransactionContext context,
            TransactionRequest request,
            Connection connection,
            Callback callback,
            String contractAddress,
            String groupId,
            String chainId,
            long blockNumber,
            CryptoKeyPair credentials,
            Path path,
            String name) {
        asyncBfsService.queryABI(
                name,
                this,
                connection,
                (queryABIException, abi) -> {
                    try {
                        if (Objects.nonNull(queryABIException)) {
                            throw new BCOSStubException(
                                    BCOSStatusCode.ABINotExist, queryABIException.getMessage());
                        }

                        if (abi == null) {
                            throw new BCOSStubException(
                                    BCOSStatusCode.ABINotExist, "resource:" + name + " not exist");
                        }

                        // encode
                        String[] args = request.getArgs();
                        String method = request.getMethod();
                        ContractABIDefinition contractABIDefinition =
                                abiDefinitionFactory.loadABI(abi);

                        List<ABIDefinition> functions =
                                contractABIDefinition.getFunctions().get(method);
                        if (Objects.isNull(functions) || functions.isEmpty()) {
                            throw new BCOSStubException(
                                    BCOSStatusCode.MethodNotExist,
                                    "Method not found in abi, method: " + method);
                        }

                        ABIObject inputObj = ABIObjectFactory.createInputObject(functions.get(0));

                        byte[] encodedArgs = new byte[0];
                        if (!Objects.isNull(args)) {
                            ABIObject encodedObj =
                                    contractCodecJsonWrapper.encode(inputObj, Arrays.asList(args));
                            encodedArgs = encodedObj.encode(isWasm);
                        }

                        String uniqueID =
                                (String)
                                        request.getOptions()
                                                .get(StubConstant.TRANSACTION_UNIQUE_ID);
                        String uid =
                                Objects.nonNull(uniqueID)
                                        ? uniqueID
                                        : UUID.randomUUID().toString().replaceAll("-", "");

                        String transactionID =
                                (String) request.getOptions().get(StubConstant.XA_TRANSACTION_ID);

                        Long transactionSeq =
                                (Long) request.getOptions().get(StubConstant.XA_TRANSACTION_SEQ);
                        Long seq = Objects.isNull(transactionSeq) ? 0L : transactionSeq;

                        Function function;
                        if (Objects.isNull(transactionID)
                                || transactionID.isEmpty()
                                || "0".equals(transactionID)) {
                            function =
                                    FunctionUtility.newSendTransactionProxyFunction(
                                            functionEncoder,
                                            uid,
                                            path.getResource(),
                                            functions.get(0).getMethodSignatureAsString(),
                                            encodedArgs);
                        } else {
                            function =
                                    FunctionUtility.newSendTransactionProxyFunction(
                                            uid,
                                            transactionID,
                                            seq,
                                            path.toString(),
                                            functions.get(0).getMethodSignatureAsString(),
                                            encodedArgs);
                        }

                        byte[] encodedAbi = functionEncoder.encode(function);
                        // get signed transaction hex string
                        TxPair signedTransaction =
                                TransactionBuilderJniObj.createSignedTransaction(
                                        credentials.getJniKeyPair(),
                                        groupId,
                                        chainId,
                                        contractAddress,
                                        Hex.toHexString(encodedAbi),
                                        abi,
                                        blockNumber + 1000,
                                        0);
                        String signTx = signedTransaction.getSignedTx();

                        TransactionParams transaction =
                                new TransactionParams(
                                        request,
                                        signTx,
                                        TransactionParams.SUB_TYPE.SEND_TX_BY_PROXY);

                        transaction.setAbi(abi);
                        Request req =
                                Request.newRequest(
                                        BCOSRequestType.SEND_TRANSACTION,
                                        objectMapper.writeValueAsBytes(transaction));

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "asyncSendTransactionByProxy, uid: {}, tid: {}, seq: {}, path: {}, abi: {}",
                                    uid,
                                    transactionID,
                                    seq,
                                    path,
                                    abi);
                        }
                        sendTxWithEncodedRequest(context, connection, callback, functions, req);

                    } catch (BCOSStubException e) {
                        logger.warn(" e: ", e);
                        callback.onTransactionResponse(
                                new TransactionException(e.getErrorCode(), e.getMessage()), null);
                    } catch (Exception e) {
                        logger.warn(" e: ", e);
                        callback.onTransactionResponse(
                                new TransactionException(
                                        BCOSStatusCode.UnclassifiedError, e.getMessage()),
                                null);
                    }
                });
    }

    private void sendTxWithEncodedRequest(
            TransactionContext context,
            Connection connection,
            Callback callback,
            List<ABIDefinition> functions,
            Request req) {
        connection.asyncSend(
                req,
                response -> {
                    try {
                        TransactionResponse transactionResponse = new TransactionResponse();
                        if (response.getErrorCode() != BCOSStatusCode.Success) {
                            throw new BCOSStubException(
                                    response.getErrorCode(), response.getErrorMessage());
                        }

                        TransactionReceipt receipt =
                                objectMapper.readValue(
                                        response.getData(), TransactionReceipt.class);
                        if (logger.isDebugEnabled()) {
                            logger.debug("TransactionReceipt: {}", receipt);
                        }

                        if (receipt.isStatusOK()) {

                            BlockManager blockManager = context.getBlockManager();

                            blockManager.asyncGetBlock(
                                    receipt.getBlockNumber().longValue(),
                                    (blockException, block) -> {
                                        try {
                                            if (Objects.nonNull(blockException)) {
                                                callback.onTransactionResponse(
                                                        new TransactionException(
                                                                BCOSStatusCode
                                                                        .HandleGetBlockNumberFailed,
                                                                blockException.getMessage()),
                                                        null);
                                                return;
                                            }

                                            MerkleValidation.verifyTransactionReceiptProof(
                                                    receipt.getTransactionHash(),
                                                    block.getBlockHeader(),
                                                    receipt,
                                                    cryptoSuite);

                                            transactionResponse.setBlockNumber(
                                                    receipt.getBlockNumber().longValue());
                                            transactionResponse.setHash(
                                                    receipt.getTransactionHash());
                                            // decode 130
                                            String output = receipt.getOutput().substring(130);

                                            ABIObject outputObj =
                                                    ABIObjectFactory.createOutputObject(
                                                            functions.get(0));
                                            transactionResponse.setResult(
                                                    contractCodecJsonWrapper
                                                            .decode(
                                                                    outputObj,
                                                                    Hex.decode(output),
                                                                    isWasm)
                                                            .toArray(new String[0]));

                                            transactionResponse.setErrorCode(
                                                    BCOSStatusCode.Success);
                                            transactionResponse.setMessage(
                                                    BCOSStatusCode.getStatusMessage(
                                                            BCOSStatusCode.Success));
                                            callback.onTransactionResponse(
                                                    null, transactionResponse);
                                            if (logger.isDebugEnabled()) {
                                                logger.debug(
                                                        " hash: {}, response: {}",
                                                        receipt.getTransactionHash(),
                                                        transactionResponse);
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
                                                            e.getMessage()),
                                                    null);
                                        }
                                    });

                        } else {
                            transactionResponse.setErrorCode(
                                    BCOSStatusCode.SendTransactionNotSuccessStatus);
                            if (receipt.getStatus()
                                    == TransactionReceiptStatus.RevertInstruction.getCode()) {
                                Tuple2<Boolean, String> booleanStringTuple2 =
                                        RevertMessage.tryParserRevertMessage(
                                                receipt.getStatus(), receipt.getOutput());
                                if (booleanStringTuple2.getValue1()) {
                                    transactionResponse.setMessage(booleanStringTuple2.getValue2());
                                } else {
                                    // return revert message
                                    transactionResponse.setMessage(receipt.getMessage());
                                }
                            } else {
                                transactionResponse.setMessage(
                                        TransactionReceiptStatus.getStatusMessage(
                                                        receipt.getStatus(), "Unknown error")
                                                .getMessage());
                            }

                            callback.onTransactionResponse(null, transactionResponse);
                        }
                    } catch (BCOSStubException e) {
                        logger.warn(" e: ", e);
                        callback.onTransactionResponse(
                                new TransactionException(e.getErrorCode(), e.getMessage()), null);
                    } catch (Exception e) {
                        logger.warn(" e: ", e);
                        callback.onTransactionResponse(
                                new TransactionException(
                                        BCOSStatusCode.UnclassifiedError, e.getMessage()),
                                null);
                    }
                });
    }

    @Override
    public void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback) {

        Request request = Request.newRequest(BCOSRequestType.GET_BLOCK_NUMBER, "");
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
    public void asyncGetBlock(
            long blockNumber,
            boolean onlyHeader,
            Connection connection,
            GetBlockCallback callback) {

        Request request =
                Request.newRequest(
                        BCOSRequestType.GET_BLOCK_BY_NUMBER,
                        BigInteger.valueOf(blockNumber).toByteArray());

        String blockVerifierString = connection.getProperties().get(BCOSConstant.BCOS_SEALER_LIST);

        connection.asyncSend(
                request,
                response -> {
                    if (response.getErrorCode() != 0) {
                        logger.warn(
                                " asyncGetBlock, errorCode: {},  errorMessage: {}",
                                response.getErrorCode(),
                                response.getErrorMessage());

                        callback.onResponse(new Exception(response.getErrorMessage()), null);
                    } else {
                        try {
                            Block block = BlockUtility.convertToBlock(response.getData(), false);
                            if (blockVerifierString != null && blockNumber != 0) {
                                BCOSBlockHeader bcosBlockHeader =
                                        (BCOSBlockHeader) block.blockHeader;
                                BlockHeaderValidation.verifyBlockHeader(
                                        bcosBlockHeader,
                                        blockVerifierString,
                                        connection.getProperties().get(BCOSConstant.BCOS_STUB_TYPE),
                                        cryptoSuite);
                            }
                            callback.onResponse(null, block);
                        } catch (Exception e) {
                            logger.warn(" blockNumber: {}, e: ", blockNumber, e);
                            callback.onResponse(e, null);
                        }
                    }
                });
    }

    @Override
    public void asyncGetTransaction(
            String transactionHash,
            long blockNumber,
            BlockManager blockManager,
            boolean isVerified,
            Connection connection,
            GetTransactionCallback callback) {
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

                    if (blockNumber != proof.getReceiptWithProof().getBlockNumber().longValue()) {
                        callback.onResponse(
                                new Exception("Transaction hash does not match the block number"),
                                null);
                        return;
                    }

                    TransactionReceipt transactionReceipt = proof.getReceiptWithProof();
                    JsonTransactionResponse transaction = proof.getTransWithProof();

                    if (isVerified) {
                        MerkleValidation.verifyTransactionProof(
                                blockNumber,
                                transactionHash,
                                blockManager,
                                proof,
                                verifyException -> {
                                    if (Objects.nonNull(verifyException)) {
                                        callback.onResponse(verifyException, null);
                                        return;
                                    }
                                    assembleTransaction(
                                            transactionHash,
                                            transaction,
                                            transactionReceipt,
                                            connection,
                                            callback);
                                },
                                cryptoSuite);
                    } else {
                        assembleTransaction(
                                transactionHash,
                                transaction,
                                transactionReceipt,
                                connection,
                                callback);
                    }
                });
    }

    private void assembleTransaction(
            String transactionHash,
            JsonTransactionResponse jsonTransactionResponse,
            TransactionReceipt receipt,
            Connection connection,
            GetTransactionCallback callback) {

        try {
            byte[] txBytes =
                    ObjectMapperFactory.getObjectMapper()
                            .writeValueAsBytes(jsonTransactionResponse);
            byte[] receiptBytes = ObjectMapperFactory.getObjectMapper().writeValueAsBytes(receipt);

            String methodId;
            String input;
            String xaTransactionID = "0";
            long xaTransactionSeq = 0;
            String path;
            String resource;

            Transaction transaction = new Transaction();
            transaction.setReceiptBytes(receiptBytes);
            transaction.setTxBytes(txBytes);
            transaction.setAccountIdentity(receipt.getFrom());
            transaction.setTransactionByProxy(true);

            transaction.getTransactionResponse().setHash(transactionHash);
            transaction
                    .getTransactionResponse()
                    .setBlockNumber(receipt.getBlockNumber().longValue());

            String proxyInput = receipt.getInput();
            String proxyOutput = receipt.getOutput();
            if (proxyInput.startsWith(
                    Hex.toHexStringWithPrefix(
                            functionEncoder.buildMethodId(FunctionUtility.ProxySendTXMethod)))) {
                Tuple3<String, String, byte[]> proxyResult =
                        FunctionUtility.getSendTransactionProxyWithoutTxIdFunctionInput(proxyInput);
                resource = proxyResult.getValue2();
                input = Numeric.toHexString(proxyResult.getValue3());
                methodId = input.substring(0, FunctionUtility.MethodIDWithHexPrefixLength);
                input = input.substring(FunctionUtility.MethodIDWithHexPrefixLength);

                if (logger.isDebugEnabled()) {
                    logger.debug("  resource: {}, methodId: {}", resource, methodId);
                }
            } else if (proxyInput.startsWith(
                    Hex.toHexStringWithPrefix(
                            functionEncoder.buildMethodId(
                                    FunctionUtility.ProxySendTransactionTXMethod)))) {
                Tuple6<String, String, BigInteger, String, String, byte[]> proxyInputResult =
                        FunctionUtility.getSendTransactionProxyFunctionInput(proxyInput);

                xaTransactionID = proxyInputResult.getValue2();
                xaTransactionSeq = proxyInputResult.getValue3().longValue();
                path = proxyInputResult.getValue4();
                resource = Path.decode(path).getResource();
                String methodSig = proxyInputResult.getValue5();
                input = Numeric.toHexString(proxyInputResult.getValue6());
                methodId = Hex.toHexString(functionEncoder.buildMethodId(methodSig));

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "xaTransactionID: {}, xaTransactionSeq: {}, path: {}, methodSig: {}, methodId: {}",
                            xaTransactionID,
                            xaTransactionSeq,
                            path,
                            methodSig,
                            methodId);
                }
            } else {
                // transaction not send by proxy
                transaction.setTransactionByProxy(false);
                callback.onResponse(null, transaction);
                return;
            }

            transaction
                    .getTransactionRequest()
                    .getOptions()
                    .put(StubConstant.XA_TRANSACTION_ID, xaTransactionID);
            transaction
                    .getTransactionRequest()
                    .getOptions()
                    .put(StubConstant.XA_TRANSACTION_SEQ, xaTransactionSeq);
            transaction.setResource(resource);

            // query ABI
            String finalMethodId = methodId;
            String finalInput = input;
            asyncBfsService.queryABI(
                    resource,
                    this,
                    connection,
                    (queryABIException, abi) -> {
                        if (Objects.nonNull(queryABIException)) {
                            logger.error(
                                    "Query abi failed, transactionHash: {}, e: ",
                                    transactionHash,
                                    queryABIException);
                            callback.onResponse(null, transaction);
                            return;
                        }

                        ABIDefinition function =
                                abiDefinitionFactory
                                        .loadABI(abi)
                                        .getMethodIDToFunctions()
                                        .get(ByteBuffer.wrap(Hex.decode(finalMethodId)));

                        if (Objects.isNull(function)) {
                            logger.warn(
                                    "Maybe abi is upgraded, Load function failed, methodId: {}",
                                    finalMethodId);

                            callback.onResponse(null, transaction);
                            return;
                        }

                        ABIObject inputObject = ABIObjectFactory.createInputObject(function);

                        try {
                            List<String> inputParams =
                                    contractCodecJsonWrapper.decode(
                                            inputObject, Hex.decode(finalInput), isWasm);

                            transaction.getTransactionRequest().setMethod(function.getName());
                            /** decode input args from input */
                            transaction
                                    .getTransactionRequest()
                                    .setArgs(inputParams.toArray(new String[0]));

                            /** set error code and error message info */
                            transaction
                                    .getTransactionResponse()
                                    .setMessage(
                                            TransactionReceiptStatus.getStatusMessage(
                                                            receipt.getStatus(), "Unknown error")
                                                    .getMessage());

                            if (TransactionReceiptStatus.Success.getCode() == receipt.getStatus()) {
                                ABIObject outputObject =
                                        ABIObjectFactory.createOutputObject(function);
                                List<String> outputParams =
                                        contractCodecJsonWrapper.decode(
                                                outputObject,
                                                Hex.decode(proxyOutput.substring(130)),
                                                isWasm);
                                /** decode output from output */
                                transaction
                                        .getTransactionResponse()
                                        .setResult(outputParams.toArray(new String[0]));
                            }
                        } catch (ClassNotFoundException e) {
                            logger.error("CodecJsonWrapper e:", e);
                            callback.onResponse(e, null);
                        }

                        transaction.getTransactionResponse().setErrorCode(receipt.getStatus());
                        if (logger.isTraceEnabled()) {
                            logger.trace(
                                    "transactionHash: {}, transaction: {}",
                                    transactionHash,
                                    transaction);
                        }

                        callback.onResponse(null, transaction);
                    });
        } catch (Exception e) {
            logger.warn("transactionHash: {}, exception: ", transactionHash, e);
            callback.onResponse(e, null);
        }
    }

    private interface RequestTransactionProofCallback {
        void onResponse(BCOSStubException e, TransactionProof proof);
    }

    private void asyncRequestTransactionProof(
            String transactionHash,
            Connection connection,
            RequestTransactionProofCallback callback) {

        Request request =
                Request.newRequest(BCOSRequestType.GET_TRANSACTION_PROOF, transactionHash);
        connection.asyncSend(
                request,
                response -> {
                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Request proof, transactionHash: {}", transactionHash);
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
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    " transactionHash: {}, transactionProof: {}",
                                    transactionHash,
                                    transactionProof);
                        }

                        callback.onResponse(null, transactionProof);
                    } catch (Exception e) {
                        callback.onResponse(
                                new BCOSStubException(
                                        BCOSStatusCode.UnclassifiedError, e.getMessage()),
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
            BlockManager blockManager,
            Connection connection,
            CustomCommandCallback callback) {
        CommandHandler commandHandler = commandHandlerDispatcher.matchCommandHandler(command);
        if (Objects.isNull(commandHandler)) {
            callback.onResponse(new Exception("Command not found, command: " + command), null);
            return;
        }
        commandHandler.handle(path, args, account, blockManager, connection, callback, cryptoSuite);
    }

    @Override
    public byte[] accountSign(Account account, byte[] message) {
        if (!(account instanceof BCOSAccount)) {
            throw new UnsupportedOperationException(
                    "Not BCOSAccount, account name: " + account.getClass().getName());
        }

        CryptoKeyPair credentials = ((BCOSAccount) account).getCredentials();
        byte[] sign = signer.sign(credentials, message);
        return sign;
    }

    @Override
    public boolean accountVerify(String identity, byte[] signBytes, byte[] message) {
        boolean verify = signer.verifyBySrcData(signBytes, message, identity);
        return verify;
    }

    public void checkProperties(Map<String, String> properties) throws BCOSStubException {
        if (!properties.containsKey(BCOSConstant.BCOS_PROXY_NAME)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter,
                    "Proxy contract address not found, resource: " + BCOSConstant.BCOS_PROXY_NAME);
        }

        if (!properties.containsKey(BCOSConstant.BCOS_GROUP_ID)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter,
                    "Group id not found, resource: " + BCOSConstant.BCOS_GROUP_ID);
        }

        if (!properties.containsKey(BCOSConstant.BCOS_CHAIN_ID)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter,
                    "Chain id not found, resource: " + BCOSConstant.BCOS_CHAIN_ID);
        }
    }

    /**
     * check request field valid
     *
     * @param request
     * @throws BCOSStubException
     */
    public void checkTransactionRequest(TransactionContext context, TransactionRequest request)
            throws BCOSStubException {
        if (Objects.isNull(context)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter, "TransactionContext is null");
        }

        if (Objects.isNull(request)) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter, "TransactionRequest is null");
        }

        if (Objects.isNull(context.getBlockManager())) {
            throw new BCOSStubException(
                    BCOSStatusCode.InvalidParameter, "BlockHeaderManager is null");
        }

        if (Objects.isNull(context.getAccount())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "Account is null");
        }

        if (Objects.isNull(request.getMethod()) || "".equals(request.getMethod())) {
            throw new BCOSStubException(BCOSStatusCode.InvalidParameter, "Method is null");
        }
    }

    public CommandHandlerDispatcher getCommandHandlerDispatcher() {
        return commandHandlerDispatcher;
    }

    public void setCommandHandlerDispatcher(CommandHandlerDispatcher commandHandlerDispatcher) {
        this.commandHandlerDispatcher = commandHandlerDispatcher;
    }

    public AsyncBfsService getAsyncBfsService() {
        return asyncBfsService;
    }

    public void setAsyncBfsService(AsyncBfsService asyncBfsService) {
        this.asyncBfsService = asyncBfsService;
    }
}
