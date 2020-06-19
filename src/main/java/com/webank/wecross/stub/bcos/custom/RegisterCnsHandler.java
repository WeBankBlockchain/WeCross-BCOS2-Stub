package com.webank.wecross.stub.bcos.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.verify.MerkleValidation;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterCnsHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegisterCnsHandler.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    /** @param args version || address || abi */
    @Override
    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            Map<String, String> abiMap,
            Driver.CustomCommandCallback callback) {
        if (Objects.isNull(args) || args.length < 3) {
            callback.onResponse(
                    new TransactionException(
                            BCOSStatusCode.RegisterContractFailed, "incomplete args"),
                    null);
            return;
        }

        String name = path.toString().split("\\.")[2];
        String version = (String) args[0];
        String address = (String) args[1];
        String abi = (String) args[2];

        Map<String, String> properties = connection.getProperties();
        int groupId = Integer.parseInt(properties.get(BCOSConstant.BCOS_GROUP_ID));
        int chainId = Integer.parseInt(properties.get(BCOSConstant.BCOS_CHAIN_ID));

        blockHeaderManager.asyncGetBlockNumber(
                (exception, blockNumber) -> {
                    if (Objects.nonNull(exception)) {
                        callback.onResponse(new Exception("getting block number failed"), null);
                        return;
                    }

                    BCOSAccount bcosAccount = (BCOSAccount) account;
                    Credentials credentials = bcosAccount.getCredentials();

                    Function function =
                            new Function(
                                    BCOSConstant.CNS_METHOD_INSERT,
                                    Arrays.<Type>asList(
                                            new Utf8String(name),
                                            new Utf8String(version),
                                            new Utf8String(address),
                                            new Utf8String(abi)),
                                    Collections.<TypeReference<?>>emptyList());

                    // get signed transaction hex string
                    String signTx =
                            SignTransaction.sign(
                                    credentials,
                                    BCOSConstant.CNS_PRECOMPILED_ADDRESS,
                                    BigInteger.valueOf(groupId),
                                    BigInteger.valueOf(chainId),
                                    BigInteger.valueOf(blockNumber),
                                    FunctionEncoder.encode(function));

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "register cns, name: {}, version: {}, address: {}, abi: {}, blockNumber: {}",
                                name,
                                version,
                                address,
                                abi,
                                blockNumber);
                    }

                    TransactionRequest transactionRequest = new TransactionRequest();
                    transactionRequest.setMethod(BCOSConstant.CNS_METHOD_INSERT);
                    transactionRequest.setArgs(new String[] {name, version, address, abi});
                    TransactionParams transaction =
                            new TransactionParams(new TransactionRequest(), signTx);
                    Request request;
                    try {
                        request = requestBuilder(objectMapper.writeValueAsBytes(transaction));
                    } catch (Exception e) {
                        logger.warn("exception occurs", e);
                        callback.onResponse(e, null);
                        return;
                    }
                    connection.asyncSend(
                            request,
                            response -> {
                                try {
                                    if (response.getErrorCode() != BCOSStatusCode.Success) {
                                        throw new BCOSStubException(
                                                response.getErrorCode(),
                                                response.getErrorMessage());
                                    }

                                    TransactionReceipt receipt =
                                            objectMapper.readValue(
                                                    response.getData(), TransactionReceipt.class);

                                    if (receipt.isStatusOK()) {
                                        blockHeaderManager.asyncGetBlockHeader(
                                                receipt.getBlockNumber().longValue(),
                                                (blockHeaderException, blockHeader) -> {
                                                    if (Objects.nonNull(blockHeaderException)) {
                                                        callback.onResponse(
                                                                new Exception(
                                                                        "getting block header failed"),
                                                                null);
                                                        return;
                                                    }
                                                    MerkleValidation merkleValidation =
                                                            new MerkleValidation();
                                                    try {
                                                        merkleValidation
                                                                .verifyTransactionReceiptProof(
                                                                        receipt.getBlockNumber()
                                                                                .longValue(),
                                                                        receipt
                                                                                .getTransactionHash(),
                                                                        blockHeader,
                                                                        receipt);
                                                    } catch (BCOSStubException e) {
                                                        logger.warn(
                                                                "verifying transaction of register failed",
                                                                e);
                                                        callback.onResponse(
                                                                new Exception(
                                                                        "verifying transaction of register failed"),
                                                                null);
                                                        return;
                                                    }

                                                    // store abi
                                                    abiMap.put(path.toString(), abi);

                                                    callback.onResponse(null, "success");
                                                });
                                    } else {
                                        callback.onResponse(
                                                new Exception(receipt.getStatus()), null);
                                    }
                                } catch (Exception e) {
                                    logger.warn("exception occurs", e);
                                    callback.onResponse(e, null);
                                }
                            });
                });
    }

    private Request requestBuilder(byte[] content) {
        Request request = new Request();
        request.setType(BCOSRequestType.SEND_TRANSACTION);
        request.setData(content);
        return request;
    }
}
