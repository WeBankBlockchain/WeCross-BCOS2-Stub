package com.webank.wecross.stub.bcos.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.*;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.verify.MerkleValidation;
import java.io.*;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployContractHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeployContractHandler.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    /** @param args fileBytes || version */
    @Override
    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            Map<String, String> abiMap,
            Driver.CustomCommandCallback callback) {
        String name = path.toString().split("\\.")[2];

        if (Objects.isNull(args) || args.length < 2) {
            callback.onResponse(new Exception("incomplete args"), null);
            return;
        }

        byte[] contractBytes = BCOSUtils.objectToBytes(args[0]);
        if (Objects.isNull(contractBytes)) {
            callback.onResponse(new Exception("parsing contract fileBytes failed"), null);
            return;
        }

        CompilationResult.ContractMetadata metadata;
        try {
            boolean sm = EncryptType.encryptType != 0;

            // compile contract
            SolidityCompiler.Result res =
                    SolidityCompiler.compile(
                            contractBytes,
                            sm,
                            true,
                            SolidityCompiler.Options.ABI,
                            SolidityCompiler.Options.BIN,
                            SolidityCompiler.Options.INTERFACE,
                            SolidityCompiler.Options.METADATA);
            CompilationResult result = CompilationResult.parse(res.getOutput());
            metadata = result.getContract(name);
        } catch (IOException e) {
            logger.error("compiling contract failed", e);
            callback.onResponse(new Exception("compiling contract failed"), null);
            return;
        }

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

                    // get signed transaction hex string
                    String signTx =
                            SignTransaction.sign(
                                    credentials,
                                    "0x0",
                                    BigInteger.valueOf(groupId),
                                    BigInteger.valueOf(chainId),
                                    BigInteger.valueOf(blockNumber),
                                    metadata.bin);

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "deploy contract, name: {}, bin: {}, blockNumber: {}",
                                name,
                                metadata.bin,
                                blockNumber);
                    }

                    TransactionRequest transactionRequest = new TransactionRequest();
                    transactionRequest.setMethod(BCOSConstant.DEPLOY_METHOD);
                    TransactionParams transaction =
                            new TransactionParams(transactionRequest, signTx);
                    Request request;
                    try {
                        request = requestBuilder(objectMapper.writeValueAsBytes(transaction));
                    } catch (Exception e) {
                        logger.warn("exception occurs", e);
                        callback.onResponse(e, null);
                        return;
                    }

                    // sendTransaction
                    connection.asyncSend(
                            request,
                            deployResponse -> {
                                try {
                                    if (deployResponse.getErrorCode() != BCOSStatusCode.Success) {
                                        throw new BCOSStubException(
                                                deployResponse.getErrorCode(),
                                                deployResponse.getErrorMessage());
                                    }

                                    TransactionReceipt receipt =
                                            objectMapper.readValue(
                                                    deployResponse.getData(),
                                                    TransactionReceipt.class);

                                    if (receipt.isStatusOK()) {
                                        blockHeaderManager.asyncGetBlockHeader(
                                                blockNumber,
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
                                                                "verifying transaction of deploy failed",
                                                                e);
                                                        callback.onResponse(
                                                                new Exception(
                                                                        "verifying transaction of deploy failed"),
                                                                null);
                                                        return;
                                                    }

                                                    // register cns
                                                    String address = receipt.getContractAddress();
                                                    Object[] registerArgs =
                                                            new Object[] {
                                                                args[1], address, metadata.abi
                                                            };
                                                    RegisterCnsHandler registerCnsHandler =
                                                            new RegisterCnsHandler();
                                                    registerCnsHandler.handle(
                                                            path,
                                                            registerArgs,
                                                            account,
                                                            blockHeaderManager,
                                                            connection,
                                                            abiMap,
                                                            (registerException,
                                                                    registerResponse) -> {
                                                                if (Objects.nonNull(
                                                                        registerException)) {
                                                                    callback.onResponse(
                                                                            new Exception(
                                                                                    "registering cns failed: "
                                                                                            + registerException
                                                                                                    .getMessage()),
                                                                            null);
                                                                } else {
                                                                    callback.onResponse(
                                                                            null, address);
                                                                }
                                                            });
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
