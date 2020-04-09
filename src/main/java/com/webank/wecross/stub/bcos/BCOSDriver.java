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
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.VerifiedTransaction;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.contract.ProofVerifierUtility;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.crypto.Credentials;
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
            logger.error(" Exception: {}", e);
            return null;
        }
    }

    @Override
    public TransactionResponse call(
            TransactionContext<TransactionRequest> request, Connection connection) {

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
                            request.getData().getMethod(),
                            Arrays.asList(request.getData().getArgs()));

            logger.debug(
                    " name:{}, address: {}, method: {}, args: {}",
                    resourceInfo.getName(),
                    contractAddress,
                    request.getData().getMethod(),
                    request.getData().getArgs());

            Request req =
                    requestBuilder(
                            BCOSRequestType.CALL,
                            contractAddress + "," + FunctionEncoder.encode(function));
            Response resp = connection.send(req);
            if (resp.getErrorCode() != BCOSStatusCode.Success) {
                throw new BCOSStubException(resp.getErrorCode(), resp.getErrorMessage());
            }

            Call.CallOutput callOutput =
                    objectMapper.readValue(resp.getData(), Call.CallOutput.class);

            logger.debug(
                    " call result, status: {}, blk: {}",
                    callOutput.getStatus(),
                    callOutput.getCurrentBlockNumber());

            if (StatusCode.Success.equals(callOutput.getStatus())) {
                response.setErrorCode(BCOSStatusCode.Success);
                response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
                response.setResult(FunctionUtility.decodeOutput(callOutput.getOutput()));
            } else {
                response.setErrorCode(BCOSStatusCode.CallNotSuccessStatus);
                response.setErrorMessage(StatusCode.getStatusMessage(callOutput.getStatus()));
            }

        } catch (BCOSStubException e) {
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn(" Exception: {}", e);
            response.setErrorCode(BCOSStatusCode.UnclassifiedError);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }

        logger.trace(
                " errorCode: {}, errorMessage: {}, output: {}",
                response.getErrorCode(),
                response.getErrorMessage(),
                response.getResult());

        return response;
    }

    @Override
    public TransactionResponse sendTransaction(
            TransactionContext<TransactionRequest> request, Connection connection) {

        TransactionResponse response = new TransactionResponse();

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
                            request.getData().getMethod(),
                            Arrays.asList(request.getData().getArgs()));

            logger.debug(
                    " contractAddress: {}, blockNumber: {}, method: {}, args: {}",
                    contractAddress,
                    blockNumber,
                    request.getData().getMethod(),
                    request.getData().getArgs());

            // get signed transaction hex string
            String signTx =
                    SignTransaction.sign(
                            credentials,
                            contractAddress,
                            BigInteger.valueOf(groupId),
                            BigInteger.valueOf(chainId),
                            BigInteger.valueOf(blockNumber),
                            FunctionEncoder.encode(function));

            Request req = requestBuilder(BCOSRequestType.SEND_TRANSACTION, signTx);
            Response resp = connection.send(req);
            if (resp.getErrorCode() != BCOSStatusCode.Success) {
                throw new BCOSStubException(resp.getErrorCode(), resp.getErrorMessage());
            }

            TransactionProof transactionProof =
                    objectMapper.readValue(resp.getData(), TransactionProof.class);
            TransactionReceipt receipt =
                    transactionProof.getReceiptAndProof().getTransactionReceipt();

            verifyTransactionProof(
                    receipt.getBlockNumber().longValue(),
                    request.getBlockHeaderManager(),
                    transactionProof);

            response.setBlockNumber(receipt.getBlockNumber().longValue());
            response.setHash(receipt.getTransactionHash());
            response.setResult(FunctionUtility.decodeOutput(receipt));

            if (receipt.isStatusOK()) {
                response.setErrorCode(BCOSStatusCode.Success);
                response.setErrorMessage(BCOSStatusCode.getStatusMessage(BCOSStatusCode.Success));
            } else {
                response.setErrorCode(BCOSStatusCode.SendTransactionNotSuccessStatus);
                response.setErrorMessage(StatusCode.getStatusMessage(receipt.getStatus()));
            }
        } catch (BCOSStubException e) {
            response.setErrorCode(e.getErrorCode());
            response.setErrorMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn(" Exception: {}", e);
            response.setErrorCode(BCOSStatusCode.UnclassifiedError);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }

        return response;
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
     * s
     *
     * @param blockNumber
     * @param blockHeaderManager
     * @param transactionProof
     * @throws BCOSStubException
     */
    public void verifyTransactionProof(
            long blockNumber,
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
        if (!ProofVerifierUtility.verify(
                blockHeader.getTransactionRoot(),
                blockHeader.getReceiptRoot(),
                transactionProof.getTransAndProof(),
                transactionProof.getReceiptAndProof())) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionProofVerifyFailed,
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.TransactionProofVerifyFailed));
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

            if (blockNumber != receipt.getBlockNumber().longValue()) {
                logger.warn(
                        " invalid blockNumber, blockNumber: {}, receipt blockNumber: {}",
                        blockNumber,
                        receipt.getBlockNumber());
                blockNumber = receipt.getBlockNumber().longValue();
            }

            verifyTransactionProof(blockNumber, blockHeaderManager, transactionProof);

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
