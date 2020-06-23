package com.webank.wecross.stub.bcos.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.AsyncCnsService;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.*;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.verify.MerkleValidation;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;
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

    private AsyncCnsService asyncCnsService = new AsyncCnsService();

    /** @param args contractBytes || version */
    @Override
    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            Map<String, String> abiMap,
            Driver.CustomCommandCallback callback) {

        if (Objects.isNull(args) || args.length < 2) {
            callback.onResponse(new Exception("incomplete args"), null);
            return;
        }

        byte[] contractBytes = Base64.getDecoder().decode((String) args[0]);
        if (Objects.isNull(contractBytes)) {
            callback.onResponse(new Exception("parsing contract fileBytes failed"), null);
            return;
        }

        String version = (String) args[1];
        String name = path.toString().split("\\.")[2];
        BCOSAccount bcosAccount = (BCOSAccount) account;
        Credentials credentials = bcosAccount.getCredentials();

        // check version
        checkContractVersion(
                name,
                version,
                account,
                connection,
                checkVersionException -> {
                    if (Objects.nonNull(checkVersionException)) {
                        callback.onResponse(
                                new Exception(
                                        "checking contract version failed: "
                                                + checkVersionException.getMessage()),
                                null);
                        return;
                    }

                    CompilationResult.ContractMetadata metadata;
                    try {
                        boolean sm = EncryptType.encryptType != 0;

                        // save contractBytes as file temporarily
                        String tempPath = String.valueOf(System.currentTimeMillis());
                        File tempFile = new File(tempPath + ".zip");
                        Files.write(tempFile.toPath(), contractBytes);
                        BCOSFileUtils.unZip(tempPath + ".zip", tempPath + "/");
                        File desContract = new File(tempPath + "/solidity/" + name + ".sol");

                        // compile contract
                        SolidityCompiler.Result res =
                                SolidityCompiler.compile(
                                        desContract,
                                        sm,
                                        true,
                                        SolidityCompiler.Options.ABI,
                                        SolidityCompiler.Options.BIN,
                                        SolidityCompiler.Options.INTERFACE,
                                        SolidityCompiler.Options.METADATA);
                        // delete temp file
                        tempFile.delete();
                        BCOSFileUtils.deleteDir(new File(tempPath));

                        if (res.isFailed()) {
                            callback.onResponse(
                                    new Exception("compiling contract failed, " + res.getErrors()),
                                    null);
                            return;
                        }

                        CompilationResult result = CompilationResult.parse(res.getOutput());
                        metadata = result.getContract(name);
                    } catch (IOException e) {
                        logger.error("compiling contract failed", e);
                        callback.onResponse(new Exception("compiling contract failed"), null);
                        return;
                    }

                    Map<String, String> properties = connection.getProperties();
                    int groupID = Integer.parseInt(properties.get(BCOSConstant.BCOS_GROUP_ID));
                    int chainID = Integer.parseInt(properties.get(BCOSConstant.BCOS_CHAIN_ID));

                    if (logger.isDebugEnabled()) {
                        logger.debug("deploy contract, name: {}, bin: {}", name, metadata.bin);
                    }

                    // deploy contract
                    deployContract(
                            groupID,
                            chainID,
                            name,
                            metadata.bin,
                            credentials,
                            blockHeaderManager,
                            connection,
                            (deployException, address) -> {
                                if (Objects.nonNull(deployException)) {
                                    callback.onResponse(
                                            new Exception(
                                                    "deploying contract failed, "
                                                            + deployException.getMessage()),
                                            null);
                                    return;
                                }

                                // register cns
                                asyncCnsService.insert(
                                        name,
                                        address,
                                        version,
                                        metadata.abi,
                                        account,
                                        blockHeaderManager,
                                        connection,
                                        insertException -> {
                                            if (Objects.nonNull(insertException)) {
                                                callback.onResponse(
                                                        new Exception(
                                                                "registering cns failed: "
                                                                        + insertException
                                                                                .getMessage()),
                                                        null);
                                                return;
                                            }

                                            callback.onResponse(null, address);
                                            if (BCOSConstant.BCOS_PROXY_NAME.equals(name)) {
                                                // save abi
                                                try {
                                                    File abiFile =
                                                            new File(
                                                                    BCOSConstant.BCOS_PROXY_NAME
                                                                            + ".abi");
                                                    Files.write(
                                                            abiFile.toPath(),
                                                            metadata.abi.getBytes());
                                                } catch (IOException e) {
                                                    logger.warn("exception occurs", e);
                                                }
                                            }
                                        });
                            });
                });
    }

    private interface CheckContractVersionCallback {
        void onResponse(Exception e);
    }

    private void checkContractVersion(
            String name,
            String version,
            Account account,
            Connection connection,
            CheckContractVersionCallback callback) {

        asyncCnsService.selectByNameAndVersion(
                name,
                version,
                account,
                connection,
                (exception, infoList) -> {
                    if (Objects.nonNull(exception)) {
                        callback.onResponse(exception);
                        return;
                    }

                    if (Objects.nonNull(infoList) && !infoList.isEmpty()) {
                        callback.onResponse(
                                new Exception("contract name and version already exist"));
                    } else {
                        callback.onResponse(null);
                    }
                });
    }

    private interface DeployContractCallback {
        void onResponse(Exception e, String address);
    }

    private void deployContract(
            int groupID,
            int chainID,
            String name,
            String bin,
            Credentials credentials,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            DeployContractCallback callback) {
        blockHeaderManager.asyncGetBlockNumber(
                (exception, blockNumber) -> {
                    if (Objects.nonNull(exception)) {
                        callback.onResponse(
                                new Exception(
                                        "getting block number failed, " + exception.getMessage()),
                                null);
                        return;
                    }

                    // get signed transaction hex string
                    String signTx =
                            SignTransaction.sign(
                                    credentials,
                                    null,
                                    BigInteger.valueOf(groupID),
                                    BigInteger.valueOf(chainID),
                                    BigInteger.valueOf(blockNumber),
                                    bin);

                    TransactionRequest transactionRequest = new TransactionRequest();
                    transactionRequest.setMethod(BCOSConstant.METHOD_DEPLOY);
                    TransactionParams transaction =
                            new TransactionParams(transactionRequest, signTx);
                    Request request;
                    try {
                        request =
                                RequestFactory.requestBuilder(
                                        BCOSRequestType.SEND_TRANSACTION,
                                        objectMapper.writeValueAsBytes(transaction));
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
                                        callback.onResponse(
                                                new Exception(deployResponse.getErrorMessage()),
                                                null);
                                        return;
                                    }

                                    TransactionReceipt receipt =
                                            objectMapper.readValue(
                                                    deployResponse.getData(),
                                                    TransactionReceipt.class);

                                    if (receipt.isStatusOK()) {
                                        blockHeaderManager.asyncGetBlockHeader(
                                                receipt.getBlockNumber().longValue(),
                                                (blockHeaderException, blockHeader) -> {
                                                    if (Objects.nonNull(blockHeaderException)) {
                                                        callback.onResponse(
                                                                new Exception(
                                                                        "getting block header failed, "
                                                                                + blockHeaderException
                                                                                        .getMessage()),
                                                                null);
                                                        return;
                                                    }
                                                    try {
                                                        MerkleValidation merkleValidation =
                                                                new MerkleValidation();
                                                        merkleValidation
                                                                .verifyTransactionReceiptProof(
                                                                        receipt.getBlockNumber()
                                                                                .longValue(),
                                                                        receipt
                                                                                .getTransactionHash(),
                                                                        blockHeader,
                                                                        receipt);

                                                        // save address if it is proxy contract
                                                        if (BCOSConstant.BCOS_PROXY_NAME.equals(
                                                                name)) {
                                                            Map<String, String> properties =
                                                                    connection.getProperties();
                                                            properties.put(
                                                                    BCOSConstant.BCOS_PROXY_NAME,
                                                                    receipt.getContractAddress());
                                                        }

                                                        callback.onResponse(
                                                                null, receipt.getContractAddress());
                                                    } catch (BCOSStubException e) {
                                                        logger.warn(
                                                                "verifying transaction of deploy failed",
                                                                e);
                                                        callback.onResponse(
                                                                new Exception(
                                                                        "verifying transaction of deploy failed, "
                                                                                + e.getMessage()),
                                                                null);
                                                    }
                                                });
                                    } else {
                                        callback.onResponse(
                                                new Exception(receipt.getMessage()), null);
                                    }
                                } catch (Exception e) {
                                    logger.warn("exception occurs", e);
                                    callback.onResponse(e, null);
                                }
                            });
                });
    }
}
