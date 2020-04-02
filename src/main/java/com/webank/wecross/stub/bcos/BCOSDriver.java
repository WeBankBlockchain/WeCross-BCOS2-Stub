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
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
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

public class BCOSDriver implements Driver {

    private static Logger logger = LoggerFactory.getLogger(BCOSDriver.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public BCOSDriver() {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
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
        } catch (IOException e) {
            logger.warn(" IOException: {}", e);
            return null;
        }
    }

    @Override
    public TransactionResponse call(
            TransactionContext<TransactionRequest> request, Connection connection) {

        TransactionResponse response = new TransactionResponse();

        try {
            // check
            checkRequest(request);

            // get contractAddress from resourceInfo
            ResourceInfo resourceInfo = request.getResourceInfo();
            Map<Object, Object> properties = resourceInfo.getProperties();

            logger.trace(
                    " resource => name: {}, type: {}, properties: {}",
                    resourceInfo.getName(),
                    resourceInfo.getStubType(),
                    resourceInfo.getProperties());

            String contractAddress = (String) properties.get(resourceInfo.getName());
            Objects.requireNonNull(
                    contractAddress,
                    " Not found contract address, resource name: " + resourceInfo.getName());

            // Function object
            Function function =
                    FunctionUtility.newFunction(
                            request.getData().getMethod(),
                            Arrays.asList(request.getData().getArgs()));
            // ABI data
            String data = FunctionEncoder.encode(function);

            logger.debug(
                    " address: {}, method: {}, args: {}, abi: {}",
                    contractAddress,
                    request.getData().getMethod(),
                    request.getData().getArgs(),
                    data);

            Request req = new Request();
            req.setType(BCOSRequestType.CALL);
            req.setData((contractAddress + "," + data).getBytes(StandardCharsets.UTF_8));
            Response resp = connection.send(req);
            if (resp.getErrorCode() != 0) {
                throw new RuntimeException(resp.getErrorMessage());
            }

            Call.CallOutput callOutput =
                    objectMapper.readValue(resp.getData(), Call.CallOutput.class);

            logger.debug(
                    " CallOutput,  status: {}, current blk: {}",
                    callOutput.getStatus(),
                    callOutput.getCurrentBlockNumber());
            if (StatusCode.Success.equals(callOutput.getStatus())) {
                response.setErrorCode(0);
                response.setErrorMessage("success");
                response.setResult(FunctionUtility.decodeOutput(callOutput.getOutput()));
            } else {
                response.setErrorCode(-1);
                response.setErrorMessage(StatusCode.getStatusMessage(callOutput.getStatus()));
            }

        } catch (Exception e) {
            logger.warn(" Exception: {}", e);
            response.setErrorCode(-1);
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
            // check
            checkRequest(request);

            // get contractAddress, groupId, chainId from resourceInfo
            ResourceInfo resourceInfo = request.getResourceInfo();

            logger.trace(
                    " resource name: {}, type: {}, properties: {}",
                    resourceInfo.getName(),
                    resourceInfo.getStubType(),
                    resourceInfo.getProperties());

            Map<Object, Object> properties = resourceInfo.getProperties();

            // contractAddress
            String contractAddress = (String) properties.get(resourceInfo.getName());
            Objects.requireNonNull(
                    contractAddress,
                    " Not found contract address, resource name: " + resourceInfo.getName());

            Integer groupId = (Integer) properties.get(BCOSConstant.BCOS_RESOURCEINFO_GROUP_ID);
            Objects.requireNonNull(
                    groupId, " Not found groupId, resource name: " + resourceInfo.getName());

            Integer chainId = (Integer) properties.get(BCOSConstant.BCOS_RESOURCEINFO_CHAIN_ID);
            Objects.requireNonNull(
                    chainId, " Not found chainId, resource name: " + resourceInfo.getName());

            long blockNumber = request.getBlockHeaderManager().getBlockNumber();
            logger.trace(" blockNumber: {}", blockNumber);

            // BCOSAccount to get credentials to sign the transaction
            BCOSAccount bcosAccount = (BCOSAccount) request.getAccount();
            Credentials credentials = bcosAccount.getCredentials();

            // Function object
            Function function =
                    FunctionUtility.newFunction(
                            request.getData().getMethod(),
                            Arrays.asList(request.getData().getArgs()));
            // ABI data
            String data = FunctionEncoder.encode(function);

            // get signed transaction hex string
            String signTx =
                    SignTransaction.sign(
                            credentials,
                            contractAddress,
                            BigInteger.valueOf(groupId),
                            BigInteger.valueOf(chainId),
                            BigInteger.valueOf(blockNumber),
                            data);

            Request req = new Request();
            req.setType(BCOSRequestType.SEND_TRANSACTION);
            req.setData(signTx.getBytes(StandardCharsets.UTF_8));
            Response resp = connection.send(req);
            if (resp.getErrorCode() != 0) {
                throw new RuntimeException(resp.getErrorMessage());
            }

            TransactionReceipt receipt =
                    objectMapper.readValue(resp.getData(), TransactionReceipt.class);

            if (Objects.nonNull(receipt.getTransactionHash())
                    && (!"".equals(receipt.getTransactionHash()))) {
                response.setHash(receipt.getTransactionHash());
                response.setBlockNumber(receipt.getBlockNumber().longValue());
            }

            if (receipt.isStatusOK()) {
                response.setResult(FunctionUtility.decodeOutput(receipt));
                response.setErrorCode(0);
            } else {
                response.setErrorCode(-1);
                response.setErrorMessage(StatusCode.getStatusMessage(receipt.getStatus()));
            }
        } catch (Exception e) {
            logger.warn(" Exception: {}", e);
            response.setErrorCode(-1);
            response.setErrorMessage(" errorMessage: " + e.getMessage());
        }

        return response;
    }

    @Override
    public long getBlockNumber(Connection connection) {
        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_NUMBER);
        Response response = connection.send(request);

        // Returns an invalid value to indicate that the function performed incorrectly
        if (response.getErrorCode() != 0) {
            logger.warn(
                    " errorCode: {},  errorMessage: {}",
                    response.getErrorCode(),
                    response.getErrorMessage());
            return -1;
        }

        BigInteger blockNumber = new BigInteger(response.getData());
        logger.debug(" blockNumber: {}", blockNumber);
        return blockNumber.longValue();
    }

    @Override
    public byte[] getBlockHeader(long number, Connection connection) {

        Request request = new Request();
        request.setType(BCOSRequestType.GET_BLOCK_HEADER);
        request.setData(BigInteger.valueOf(number).toByteArray());
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
     * send getTransactionReceipt request
     *
     * @param transactionHash
     * @param connection
     * @return
     * @throws IOException
     */
    public TransactionReceipt requestTransactionReceipt(
            String transactionHash, Connection connection) throws IOException {
        Request request = new Request();
        request.setType(BCOSRequestType.GET_TRANSACTION_RECEIPT);
        request.setData(transactionHash.getBytes(StandardCharsets.UTF_8));

        Response resp = connection.send(request);
        if (resp.getErrorCode() != 0) {
            throw new RuntimeException(resp.getErrorMessage());
        }

        TransactionReceipt receipt =
                objectMapper.readValue(resp.getData(), TransactionReceipt.class);

        logger.debug(" hash: {}, receipt: {}", transactionHash, receipt);
        return receipt;
    }

    @Override
    public VerifiedTransaction getVerifiedTransaction(
            String transactionHash,
            long blockNumber,
            BlockHeaderManager blockHeaderManager,
            Connection connection) {
        try {
            // get transaction receipt first
            TransactionReceipt receipt = requestTransactionReceipt(transactionHash, connection);

            /**
             * set args for TransactionRequest, the method parameter cannot be recovered from the
             * abi encoding
             */
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
            logger.warn(" Exception: {}", e);
            return null;
        }
    }

    private void checkRequest(TransactionContext<TransactionRequest> request) {
        if (request.getAccount() == null) {
            throw new InvalidParameterException("Unknown account");
        }

        if (request.getBlockHeaderManager() == null) {
            throw new InvalidParameterException("blockHeaderManager is null");
        }

        if (request.getResourceInfo() == null) {
            throw new InvalidParameterException("resourceInfo is null");
        }

        if (request.getData() == null) {
            throw new InvalidParameterException("TransactionRequest is null");
        }
    }
}
