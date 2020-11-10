package com.webank.wecross.stub.bcos.preparation;

import com.webank.wecross.stub.bcos.BCOSBaseStubFactory;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSConnectionFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.contract.SignTransaction;
import com.webank.wecross.stub.bcos.web3j.Web3jUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImpl;
import java.io.File;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.precompile.cns.CnsService;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.precompile.common.PrecompiledResponse;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ProxyContract {

    private static final Logger logger = LoggerFactory.getLogger(ProxyContract.class);

    private String proxyContractFile;
    private String chainPath;

    private BCOSAccount account;
    private BCOSConnection connection;

    public ProxyContract(String proxyContractFile, String chainPath, String accountName)
            throws Exception {
        this.proxyContractFile = proxyContractFile;
        this.chainPath = chainPath;
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser(chainPath, "stub.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        boolean isGM = bcosStubConfig.getType().toLowerCase().contains("gm");

        Web3j web3j = Web3jUtility.initWeb3j(bcosStubConfig);
        Web3jWrapper web3jWrapper = new Web3jWrapperImpl(web3j);
        BCOSBaseStubFactory bcosBaseStubFactory =
                isGM
                        ? new BCOSBaseStubFactory(EncryptType.SM2_TYPE, "sm2p256v1", "GM_BCOS2.0")
                        : new BCOSBaseStubFactory(EncryptType.ECDSA_TYPE, "secp256k1", "BCOS2.0");

        account =
                (BCOSAccount)
                        bcosBaseStubFactory.newAccount(
                                accountName, "classpath:accounts" + File.separator + accountName);
        connection = BCOSConnectionFactory.build(bcosStubConfig, web3jWrapper);
        if (account == null) {
            throw new Exception("Account " + accountName + " not found");
        }

        if (connection == null) {
            throw new Exception("Init connection exception, please check log");
        }

        if (!bcosStubConfig.getType().equals(account.getType())) {
            throw new Exception(
                    "Account type "
                            + account.getType()
                            + " and chain type "
                            + bcosStubConfig.getType()
                            + " are not the same.");
        }
    }

    public ProxyContract() {}

    public BCOSAccount getAccount() {
        return account;
    }

    public void setAccount(BCOSAccount account) {
        this.account = account;
    }

    public BCOSConnection getConnection() {
        return connection;
    }

    public void setConnection(BCOSConnection connection) {
        this.connection = connection;
    }

    /**
     * @param solFile, String contractName
     * @return
     */
    public CnsInfo deployContractAndRegisterCNS(
            File solFile, String contractName, String cnsName, String cnsVersion) throws Exception {

        logger.info("cnsName: {}, cnsVersion: {}", cnsName, cnsVersion);

        /** First compile the contract source code */
        SolidityCompiler.Result res =
                SolidityCompiler.compile(
                        solFile,
                        EncryptType.encryptType == EncryptType.SM2_TYPE,
                        true,
                        SolidityCompiler.Options.ABI,
                        SolidityCompiler.Options.BIN,
                        SolidityCompiler.Options.INTERFACE,
                        SolidityCompiler.Options.METADATA);

        if (res.isFailed()) {
            throw new RuntimeException(" compiling contract failed, " + res.getErrors());
        }

        CompilationResult.ContractMetadata metadata =
                CompilationResult.parse(res.getOutput()).getContract(contractName);

        /** deploy the contract by sendTransaction */
        // groupId
        BigInteger groupID =
                new BigInteger(connection.getProperties().get(BCOSConstant.BCOS_GROUP_ID));
        // chainId
        BigInteger chainID =
                new BigInteger(connection.getProperties().get(BCOSConstant.BCOS_CHAIN_ID));

        Web3jWrapper web3jWrapper = connection.getWeb3jWrapper();
        BigInteger blockNumber = web3jWrapper.getBlockNumber();

        logger.info(
                " groupID: {}, chainID: {}, blockNumber: {}, accountAddress: {}, bin: {}, abi: {}",
                chainID,
                groupID,
                blockNumber,
                account.getCredentials().getAddress(),
                metadata.bin,
                metadata.abi);

        String signTx =
                SignTransaction.sign(
                        account.getCredentials(),
                        null,
                        groupID,
                        chainID,
                        blockNumber,
                        metadata.bin);

        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        web3jWrapper.sendTransactionAndGetProof(
                signTx,
                new TransactionSucCallback() {
                    @Override
                    public void onResponse(TransactionReceipt receipt) {
                        if (!receipt.isStatusOK()) {
                            logger.error(
                                    " deploy contract failed, error status: {}, error message: {} ",
                                    receipt.getStatus(),
                                    receipt.getMessage());
                            completableFuture.complete(null);
                        } else {
                            logger.info(
                                    " deploy contract success, contractAddress: {}",
                                    receipt.getContractAddress());
                            completableFuture.complete(receipt.getContractAddress());
                        }
                    }
                });

        String contractAddress = completableFuture.get(10, TimeUnit.SECONDS);
        CnsService cnsService = new CnsService(web3jWrapper.getWeb3j(), account.getCredentials());
        String result = cnsService.registerCns(cnsName, cnsVersion, contractAddress, metadata.abi);

        PrecompiledResponse precompiledResponse =
                ObjectMapperFactory.getObjectMapper().readValue(result, PrecompiledResponse.class);
        if (precompiledResponse.getCode() != PrecompiledCommon.Success) {
            throw new RuntimeException(" registerCns failed, error message: " + result);
        }

        CnsInfo cnsInfo = new CnsInfo(cnsName, cnsVersion, contractAddress, metadata.abi);
        return cnsInfo;
    }

    public void deploy() throws Exception {
        if (!connection.hasProxyDeployed()) {
            System.out.println("Deploy WeCrossProxy to chain " + chainPath + " ...");

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            File file = resolver.getResource("classpath:" + proxyContractFile).getFile();
            String version = String.valueOf(System.currentTimeMillis() / 1000);

            deployContractAndRegisterCNS(file, "WeCrossProxy", "WeCrossProxy", version);
            System.out.println(
                    "SUCCESS: WeCrossProxy:" + version + " has been deployed! chain: " + chainPath);
        } else {
            System.out.println(
                    "SUCCESS: WeCrossProxy has already been deployed! chain: " + chainPath);
        }
    }

    public void upgrade() throws Exception {

        System.out.println("Upgrade WeCrossProxy to chain " + chainPath + " ...");

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        File file = resolver.getResource("classpath:" + proxyContractFile).getFile();
        String version = String.valueOf(System.currentTimeMillis() / 1000);

        deployContractAndRegisterCNS(file, "WeCrossProxy", "WeCrossProxy", version);

        System.out.println(
                "SUCCESS: WeCrossProxy:" + version + " has been upgraded! chain: " + chainPath);
    }

    public static void check(String chainPath) {
        try {
            BCOSConnection connection = BCOSConnectionFactory.build(chainPath, "stub.toml");

            if (!connection.hasProxyDeployed()) {
                System.out.println("WeCrossProxy has not been deployed");
            } else {
                System.out.println("WeCrossProxy has been deployed.");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
