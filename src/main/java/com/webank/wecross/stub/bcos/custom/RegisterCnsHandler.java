package com.webank.wecross.stub.bcos.custom;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.bcos.AsyncCnsService;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.utils.Numeric;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterCnsHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegisterCnsHandler.class);

    private AsyncCnsService asyncCnsService;

    public AsyncCnsService getAsyncCnsService() {
        return asyncCnsService;
    }

    public void setAsyncCnsService(AsyncCnsService asyncCnsService) {
        this.asyncCnsService = asyncCnsService;
    }

    /** @param args version || address || abi */
    @Override
    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockManager blockManager,
            Connection connection,
            Driver.CustomCommandCallback callback) {
        if (Objects.isNull(args) || args.length < 6) {
            callback.onResponse(
                    new TransactionException(
                            BCOSStatusCode.RegisterContractFailed, "incomplete args"),
                    null);
            return;
        }

        String cnsName = (String) args[0];
        String sourceType = (String) args[1];
        String sourceContent = (String) args[2];
        String address = (String) args[3];
        String contractName = (String) args[4];
        String version = (String) args[5];

        if (logger.isDebugEnabled()) {
            logger.debug(
                    " cnsName: {}, sourceType: {}, address: {}, contractName: {}, version: {} ",
                    cnsName,
                    sourceType,
                    address,
                    contractName,
                    version);
        }

        String abi = sourceContent;

        /* Parameter calibration */
        if (version.length() > CnsService.MAX_VERSION_LENGTH) {
            callback.onResponse(
                    new Exception("The length of version field must be less than or equal to 40"),
                    null);
            return;
        }

        try {
            if (!address.startsWith("0x") || address.length() < 6) {
                throw new IllegalArgumentException("Invalid address: " + address);
            }

            address =
                    Numeric.toHexStringWithPrefixZeroPadded(
                            new Address(address).toUint160().getValue(), 40);
        } catch (Exception e) {
            logger.error("e: ", e);
            callback.onResponse(
                    new Exception("Illegal contract address format, address: " + address), null);
            return;
        }

        /** Compile the source to generate the ABI first */
        if (sourceType.equals("sol")) {
            try {
                CompilationResult.ContractMetadata metadata = null;
                boolean sm = (EncryptType.encryptType == EncryptType.SM2_TYPE);

                File sourceFile = File.createTempFile("BCOSContract-", "-" + contractName + ".sol");
                try (OutputStream outputStream = new FileOutputStream(sourceFile)) {
                    outputStream.write(sourceContent.getBytes());
                }

                // compile contract
                SolidityCompiler.Result res =
                        SolidityCompiler.compile(
                                sourceFile,
                                sm,
                                true,
                                SolidityCompiler.Options.ABI,
                                SolidityCompiler.Options.BIN,
                                SolidityCompiler.Options.INTERFACE,
                                SolidityCompiler.Options.METADATA);

                if (res.isFailed()) {
                    callback.onResponse(
                            new Exception("Compile contract failed, error: " + res.getErrors()),
                            res.getErrors());
                    return;
                }

                CompilationResult result = CompilationResult.parse(res.getOutput());
                metadata = result.getContract(contractName);
                abi = metadata.abi;
            } catch (Exception e) {
                logger.error("e: ", e);
                callback.onResponse(
                        new Exception("Compile contract exception, error: " + e.getMessage()),
                        null);
                return;
            }
        }

        String finalAbi = abi;
        String finalAddress = address;
        asyncCnsService.registerCNSByProxy(
                path,
                address,
                version,
                abi,
                account,
                blockManager,
                connection,
                e -> {
                    if (Objects.nonNull(e)) {
                        logger.warn("registering abi failed", e);
                        callback.onResponse(e, null);
                        return;
                    }

                    logger.info(
                            " register cns successfully, cnsName: {}, contractName: {}, version: {}, address: {}, abi: {}",
                            cnsName,
                            contractName,
                            version,
                            finalAddress,
                            finalAbi);

                    callback.onResponse(null, "success");
                });
    }
}
