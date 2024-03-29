package com.webank.wecross.stub.bcos.custom;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.bcos.AsyncCnsService;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.preparation.CnsService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.sdk.abi.wrapper.ABICodecJsonWrapper;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.abi.wrapper.ABIObject;
import org.fisco.bcos.sdk.abi.wrapper.ABIObjectFactory;
import org.fisco.bcos.sdk.abi.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployContractHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(DeployContractHandler.class);

    private ABICodecJsonWrapper abiCodecJsonWrapper = new ABICodecJsonWrapper();

    public AsyncCnsService asyncCnsService;

    public AsyncCnsService getAsyncCnsService() {
        return asyncCnsService;
    }

    public void setAsyncCnsService(AsyncCnsService asyncCnsService) {
        this.asyncCnsService = asyncCnsService;
    }

    public ABICodecJsonWrapper getAbiCodecJsonWrapper() {
        return abiCodecJsonWrapper;
    }

    public void setAbiCodecJsonWrapper(ABICodecJsonWrapper abiCodecJsonWrapper) {
        this.abiCodecJsonWrapper = abiCodecJsonWrapper;
    }

    /**
     * @param path rule id
     * @param args command args
     * @param account if needs to sign
     * @param blockManager if needs to verify transaction
     * @param connection chain connection
     * @param callback
     */
    @Override
    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockManager blockManager,
            Connection connection,
            Driver.CustomCommandCallback callback,
            CryptoSuite cryptoSuite) {

        if (Objects.isNull(args) || args.length < 4) {
            callback.onResponse(new Exception("incomplete args"), null);
            return;
        }

        String cnsName = (String) args[0];
        String sourceContent = (String) args[1];
        String className = (String) args[2];
        String version = (String) args[3];

        /* Parameter calibration */
        if (version.length() > CnsService.MAX_VERSION_LENGTH) {
            callback.onResponse(
                    new Exception("The length of version field must be less than or equal to 40"),
                    null);
            return;
        }

        Driver driver = getAsyncCnsService().getBcosDriver();
        /* constructor params */
        List<String> params = null;
        if (args.length > 4) {
            params = new ArrayList<>();
            for (int i = 4; i < args.length; ++i) {
                params.add((String) args[i]);
            }
        }

        /* First compile the contract source code */
        CompilationResult.ContractMetadata metadata;
        try {
            boolean sm = (cryptoSuite.getCryptoTypeConfig() == CryptoType.SM_TYPE);

            File sourceFile = File.createTempFile("BCOSContract-", "-" + cnsName + ".sol");
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
                        new Exception("compiling contract failed, " + res.getErrors()),
                        res.getErrors());
                return;
            }

            CompilationResult result = CompilationResult.parse(res.getOutput());
            metadata = result.getContract(className);
        } catch (Exception e) {
            logger.error("compiling contract failed, e: ", e);
            callback.onResponse(new Exception("compiling contract failed"), null);
            return;
        }

        ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory(cryptoSuite);
        ContractABIDefinition contractABIDefinition = abiDefinitionFactory.loadABI(metadata.abi);
        ABIDefinition constructor = contractABIDefinition.getConstructor();

        /* check if solidity constructor needs arguments */
        String paramsABI = "";
        if (!Objects.isNull(constructor)
                && !Objects.isNull(constructor.getInputs())
                && !constructor.getInputs().isEmpty()) {

            if (Objects.isNull(params)) {
                logger.error(" {} constructor needs arguments", className);
                callback.onResponse(
                        new Exception(className + " constructor needs arguments"), null);
                return;
            }

            ABIObject constructorABIObject = ABIObjectFactory.createInputObject(constructor);
            try {
                ABIObject abiObject = abiCodecJsonWrapper.encode(constructorABIObject, params);
                paramsABI = abiObject.encode();
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            " className: {}, params: {}, abi: {}",
                            className,
                            params.toArray(new String[0]),
                            paramsABI);
                }
            } catch (Exception e) {
                logger.error(
                        "{} constructor arguments encode failed, params: {}, e: ",
                        className,
                        params.toArray(new String[0]),
                        e);
                callback.onResponse(
                        new Exception(
                                className
                                        + " constructor arguments encode failed, e: "
                                        + e.getMessage()),
                        null);
                return;
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace(
                    "deploy contract, name: {}, bin: {}, abi:{}",
                    cnsName,
                    metadata.bin,
                    metadata.abi);
        }

        deployContractAndRegisterCNS(
                path,
                version,
                metadata.bin + paramsABI,
                metadata.abi,
                account,
                connection,
                driver,
                blockManager,
                (e, address) -> {
                    if (Objects.nonNull(e)) {
                        callback.onResponse(e, null);
                        return;
                    }

                    logger.info(" address: {}", address);
                    callback.onResponse(null, address);
                });
    }

    private interface DeployContractCallback {
        void onResponse(Exception e, String address);
    }

    private void deployContractAndRegisterCNS(
            Path path,
            String version,
            String bin,
            String abi,
            Account account,
            Connection connection,
            Driver driver,
            BlockManager blockManager,
            DeployContractCallback callback) {

        Path proxyPath = new Path();
        proxyPath.setResource(BCOSConstant.BCOS_PROXY_NAME);

        /* Binary data needs to be base64 encoded */
        String base64Bin =
                ABICodecJsonWrapper.Base64EncodedDataPrefix
                        + Base64.getEncoder().encodeToString(Numeric.hexStringToByteArray(bin));

        TransactionRequest transactionRequest =
                new TransactionRequest(
                        BCOSConstant.PROXY_METHOD_DEPLOY,
                        Arrays.asList(path.toString(), version, base64Bin, abi)
                                .toArray(new String[0]));

        TransactionContext transactionContext =
                new TransactionContext(account, proxyPath, new ResourceInfo(), blockManager);

        driver.asyncSendTransaction(
                transactionContext,
                transactionRequest,
                true,
                connection,
                (exception, res) -> {
                    if (Objects.nonNull(exception)) {
                        logger.error(" deployAndRegisterCNS e: ", exception);
                        callback.onResponse(exception, null);
                        return;
                    }

                    if (res.getErrorCode() != BCOSStatusCode.Success) {
                        logger.error(
                                " deployAndRegisterCNS, error: {}, message: {}",
                                res.getErrorCode(),
                                res.getMessage());
                        callback.onResponse(new Exception(res.getMessage()), null);
                        return;
                    }

                    logger.info(
                            " deployAndRegisterCNS successfully, name: {}, version: {}, res: {} ",
                            path.getResource(),
                            version,
                            res);

                    asyncCnsService.addAbiToCache(path.getResource(), abi);
                    callback.onResponse(null, res.getResult()[0]);
                });
    }
}
