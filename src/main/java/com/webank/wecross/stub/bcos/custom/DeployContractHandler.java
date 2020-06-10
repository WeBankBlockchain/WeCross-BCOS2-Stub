package com.webank.wecross.stub.bcos.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.*;
import com.webank.wecross.stub.bcos.contract.FunctionUtility;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.protocol.request.TransactionParams;
import com.webank.wecross.stub.bcos.verify.MerkleValidation;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
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
                credentials,
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

                        File tempFile = new File("BCOS_Stub_Temp.sol");
                        Files.write(tempFile.toPath(), contractBytes);

                        // compile contract
                        SolidityCompiler.Result res =
                                SolidityCompiler.compile(
                                        tempFile,
                                        sm,
                                        true,
                                        SolidityCompiler.Options.ABI,
                                        SolidityCompiler.Options.BIN,
                                        SolidityCompiler.Options.INTERFACE,
                                        SolidityCompiler.Options.METADATA);
                        // delete temp file
                        tempFile.delete();

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
                                Object[] registerArgs =
                                        new Object[] {version, address, metadata.abi};
                                RegisterCnsHandler registerCnsHandler = new RegisterCnsHandler();
                                registerCnsHandler.handle(
                                        path,
                                        registerArgs,
                                        account,
                                        blockHeaderManager,
                                        connection,
                                        abiMap,
                                        (registerException, registerResponse) -> {
                                            if (Objects.nonNull(registerException)) {
                                                callback.onResponse(
                                                        new Exception(
                                                                "registering cns failed: "
                                                                        + registerException
                                                                                .getMessage()),
                                                        null);
                                            } else {
                                                callback.onResponse(null, address);
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
            Credentials credentials,
            Connection connection,
            CheckContractVersionCallback callback) {
        Function function =
                new Function(
                        BCOSConstant.CNS_METHOD_SELECTBYNAMEANDVERSION,
                        Arrays.<Type>asList(new Utf8String(name), new Utf8String(version)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setMethod(BCOSConstant.CNS_METHOD_SELECTBYNAMEANDVERSION);
        transactionRequest.setArgs(new String[] {name, version});
        TransactionParams transaction =
                new TransactionParams(
                        transactionRequest,
                        FunctionEncoder.encode(function),
                        credentials.getAddress(),
                        BCOSConstant.CNS_PRECOMPILED_ADDRESS);
        Request request = null;
        try {
            request =
                    requestBuilder(
                            BCOSRequestType.CALL, objectMapper.writeValueAsBytes(transaction));
        } catch (Exception e) {
            logger.warn("exception occurs", e);
            callback.onResponse(e);
        }

        connection.asyncSend(
                request,
                connectionResponse -> {
                    try {
                        if (connectionResponse.getErrorCode() != BCOSStatusCode.Success) {
                            callback.onResponse(
                                    new Exception(connectionResponse.getErrorMessage()));
                            return;
                        }

                        Call.CallOutput callOutput =
                                objectMapper.readValue(
                                        connectionResponse.getData(), Call.CallOutput.class);

                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "call result, status: {}, blockNumber: {}",
                                    callOutput.getStatus(),
                                    callOutput.getCurrentBlockNumber());
                        }

                        if (StatusCode.Success.equals(callOutput.getStatus())) {
                            String cnsInfo =
                                    FunctionUtility.decodeOutputAsString(callOutput.getOutput());
                            List<CnsInfo> infoList =
                                    objectMapper.readValue(
                                            cnsInfo,
                                            objectMapper
                                                    .getTypeFactory()
                                                    .constructCollectionType(
                                                            List.class, CnsInfo.class));
                            if (Objects.nonNull(infoList) && !infoList.isEmpty()) {
                                callback.onResponse(
                                        new Exception("contract name and version already exist"));
                            } else {
                                callback.onResponse(null);
                            }
                        } else {
                            callback.onResponse(new Exception(callOutput.getStatus()));
                        }
                    } catch (Exception e) {
                        logger.warn("exception occurs", e);
                        callback.onResponse(new Exception(e.getMessage()));
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
                                requestBuilder(
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

    private Request requestBuilder(int type, byte[] content) {
        Request request = new Request();
        request.setType(type);
        request.setData(content);
        return request;
    }
}
