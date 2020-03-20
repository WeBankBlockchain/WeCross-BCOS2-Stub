package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.contract.StubFunction;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSDriver implements Driver {

    private static Logger logger = LoggerFactory.getLogger(BCOSDriver.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    /**
     * create sign transactin hex string
     *
     * @param groupId
     * @param chainId
     * @param blockNumber
     * @param contractAddress
     * @param data
     * @param credentials
     * @return
     */
    public String createSignTx(
            BigInteger groupId,
            BigInteger chainId,
            BigInteger blockNumber,
            String contractAddress,
            String data,
            Credentials credentials) {

        SignTransaction signTransaction = new SignTransaction(credentials, groupId, chainId);

        // get signed transaction hex string
        String signTx = signTransaction.sign(contractAddress, data, blockNumber);

        logger.debug(
                " contractAddress: {}, groupId: {}, chainId: {}, blockNumber: {}",
                contractAddress,
                groupId,
                chainId,
                blockNumber);

        return signTx;
    }

    @Override
    public TransactionContext<TransactionRequest> decodeTransactionRequest(byte[] data) {
        return null;
    }

    @Override
    public boolean isTransaction(Request request) {
        return (request.getType() == BCOSConstant.BCOS_SEND_TRANSACTION)
                || (request.getType() == BCOSConstant.BCOS_CALL);
    }

    @Override
    public BlockHeader decodeBlockHeader(byte[] data) {
        try {
            BlockHeader blockHeader = objectMapper.readValue(data, BlockHeader.class);
            logger.debug(" BlockHeader: {}", blockHeader);
            return blockHeader;
        } catch (IOException e) {
            logger.warn(" IOException: {}", e);
            return null;
        }
    }

    @Override
    public TransactionResponse call(
            TransactionContext<TransactionRequest> request, Connection connection) {
        // transaction request
        TransactionRequest transactionRequest = request.getData();

        // get contractAddress from resourceInfo
        ResourceInfo resourceInfo = request.getResourceInfo();
        Map<Object, Object> properties = resourceInfo.getProperties();

        logger.trace(
                " resource => name: {}, type: {}, properties: {}",
                resourceInfo.getName(),
                resourceInfo.getStubType(),
                resourceInfo.getProperties());

        TransactionResponse response = new TransactionResponse();

        try {
            String contractAddress = (String) properties.get(resourceInfo.getName());
            if (Objects.isNull(contractAddress)) {
                throw new RuntimeException(
                        " Not found contract address, resource name: " + resourceInfo.getName());
            }

            // Function object
            Function function =
                    StubFunction.newFunction(
                            request.getData().getMethod(),
                            Arrays.asList(request.getData().getArgs()));
            // ABI data
            String data = FunctionEncoder.encode(function);

            logger.debug(
                    " address: {}, method: {}, args: {}, ABI: {}",
                    contractAddress,
                    request.getData().getMethod(),
                    request.getData().getArgs(),
                    data);

            Request req = new Request();
            req.setType(BCOSConstant.BCOS_CALL);
            req.setData((contractAddress + "," + data).getBytes("UTF-8"));
            Response resp = connection.send(req);
            if (resp.getErrorCode() != 0) {
                throw new RuntimeException(resp.getErrorMessage());
            }

            Call.CallOutput callOutput =
                    objectMapper.readValue(resp.getData(), Call.CallOutput.class);
            logger.trace(
                    " CallOutput,  status: {}, current blk: {}",
                    callOutput.getStatus(),
                    callOutput.getCurrentBlockNumber());
            if (StatusCode.Success.equals(callOutput.getOutput())) {
                List<Type> typeList =
                        FunctionReturnDecoder.decode(
                                callOutput.getOutput(), function.getOutputParameters());
                List<String> outputs = StubFunction.convertToStringList(typeList);
                response.setErrorCode(0);
                response.setErrorMessage("success");
                response.setResult(outputs.toArray(new String[0]));
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
        // transaction request
        TransactionRequest transactionRequest = request.getData();

        // get contractAddress, groupId, chainId from resourceInfo
        ResourceInfo resourceInfo = request.getResourceInfo();

        logger.trace(
                " resource name: {}, type: {}, properties: {}",
                resourceInfo.getName(),
                resourceInfo.getStubType(),
                resourceInfo.getProperties());

        Map<Object, Object> properties = resourceInfo.getProperties();
        TransactionResponse response = new TransactionResponse();

        try {
            // contractAddress
            String contractAddress = (String) properties.get(resourceInfo.getName());
            BigInteger groupId =
                    (BigInteger) properties.get(BCOSConstant.BCOS_RESOURCEINFO_GROUP_ID);
            BigInteger chainId =
                    (BigInteger) properties.get(BCOSConstant.BCOS_RESOURCEINFO_CHAIN_ID);
            long blockNumber = request.getBlockHeaderManager().getBlockNumber();

            // BCOSAccount to get credentials to sign the transaction
            BCOSAccount bcosAccount = (BCOSAccount) request.getAccount();
            Credentials credentials = bcosAccount.getCredentials();

            // Function object
            Function function =
                    StubFunction.newFunction(
                            request.getData().getMethod(),
                            Arrays.asList(request.getData().getArgs()));
            // ABI data
            String data = FunctionEncoder.encode(function);

            String signTx =
                    createSignTx(
                            groupId,
                            chainId,
                            BigInteger.valueOf(blockNumber),
                            contractAddress,
                            data,
                            credentials);

            Request req = new Request();
            req.setType(BCOSConstant.BCOS_SEND_TRANSACTION);
            req.setData(signTx.getBytes("UTF-8"));
            Response resp = connection.send(req);
            if (resp.getErrorCode() != 0) {
                throw new RuntimeException(resp.getErrorMessage());
            }

            objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            TransactionReceipt receipt =
                    objectMapper.readValue(resp.getData(), TransactionReceipt.class);
            if (receipt.isStatusOK()) {
                response.setHash(receipt.getTransactionHash());
                List<Type> typeList =
                        FunctionReturnDecoder.decode(
                                receipt.getOutput(), function.getOutputParameters());
                List<String> outputs = StubFunction.convertToStringList(typeList);
                response.setResult(outputs.toArray(new String[0]));
                response.setErrorCode(0);
            } else {
                response.setHash(receipt.getTransactionHash());
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
        request.setType(BCOSConstant.BCOS_GET_BLOCK_NUMBER);
        Response response = connection.send(request);

        // error , return default value
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
        request.setType(BCOSConstant.BCOS_GET_BLOCK_HEADER);
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
}
