package com.webank.wecross.stub.bcos.preparation;

import com.webank.wecross.stub.bcos.BCOSBaseStubFactory;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSConnectionFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.client.AbstractClientWrapper;
import com.webank.wecross.stub.bcos.client.ClientWrapperFactory;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import java.io.File;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.sdk.jni.utilities.tx.TransactionBuilderJniObj;
import org.fisco.bcos.sdk.jni.utilities.tx.TxPair;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSInfo;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSService;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.TransactionReceiptStatus;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.solc.compiler.CompilationResult;
import org.fisco.solc.compiler.SolidityCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class HubContract {

    private static final Logger logger = LoggerFactory.getLogger(HubContract.class);

    private String hubContractFile;
    private String chainPath;

    private BCOSAccount account;
    private BCOSConnection connection;

    public HubContract(String hubContractFile, String chainPath, String accountName)
            throws Exception {
        this.hubContractFile = hubContractFile;
        this.chainPath = chainPath;

        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser(chainPath, "stub.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        boolean isGMStub = bcosStubConfig.isGMStub();

        BCOSBaseStubFactory bcosBaseStubFactory =
                isGMStub
                        ? new BCOSBaseStubFactory(CryptoType.SM_TYPE, "sm2p256v1", "GM_BCOS3.0")
                        : new BCOSBaseStubFactory(CryptoType.ECDSA_TYPE, "secp256k1", "BCOS3.0");

        AbstractClientWrapper clientWrapper =
                ClientWrapperFactory.createClientWrapperInstance(bcosStubConfig);

        account =
                (BCOSAccount)
                        bcosBaseStubFactory.newAccount(
                                accountName,
                                "classpath:" + chainPath + File.separator + accountName);
        if (account == null) {
            account =
                    (BCOSAccount)
                            bcosBaseStubFactory.newAccount(
                                    accountName,
                                    "classpath:accounts" + File.separator + accountName);
        }

        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("tmpBCOSConn-"));
        connection =
                BCOSConnectionFactory.build(
                        bcosStubConfig, clientWrapper, scheduledExecutorService);

        if (account == null) {
            throw new Exception("Account " + accountName + " not found");
        }

        if (connection == null) {
            throw new Exception("Init connection exception, please check log");
        }
    }

    public HubContract() {}

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
    public BFSInfo deployContractAndLinkBFS(File solFile, String contractName, String linkName)
            throws Exception {

        logger.info("linkName: {}", linkName);

        AbstractClientWrapper clientWrapper = connection.getClientWrapper();
        Client client = clientWrapper.getClient();

        /** First compile the contract source code */
        SolidityCompiler.Result res =
                SolidityCompiler.compile(
                        solFile,
                        client.getCryptoSuite().getCryptoTypeConfig() == CryptoType.SM_TYPE,
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
        String groupID = connection.getProperties().get(BCOSConstant.BCOS_GROUP_ID);
        // chainId
        String chainID = connection.getProperties().get(BCOSConstant.BCOS_CHAIN_ID);

        BigInteger blockLimit = client.getBlockLimit();

        logger.info(
                " groupID: {}, chainID: {}, blockLimit: {}, accountAddress: {}, bin: {}, abi: {}",
                chainID,
                groupID,
                blockLimit,
                account.getCredentials().getAddress(),
                metadata.bin,
                metadata.abi);

        CryptoKeyPair credentials = account.getCredentials();

        TxPair signedTransaction =
                TransactionBuilderJniObj.createSignedTransaction(
                        credentials.getJniKeyPair(),
                        groupID,
                        chainID,
                        "",
                        metadata.abi,
                        metadata.abi,
                        blockLimit.longValue(),
                        0);
        String signTx = signedTransaction.getSignedTx();

        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        clientWrapper.sendTransaction(
                signTx,
                new TransactionCallback() {
                    @Override
                    public void onResponse(TransactionReceipt receipt) {
                        if (!receipt.isStatusOK()) {
                            logger.error(
                                    " deploy contract failed, error status: {}, error message: {} ",
                                    receipt.getStatus(),
                                    TransactionReceiptStatus.getStatusMessage(
                                                    receipt.getStatus(), "Unknown error")
                                            .getMessage());
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
        if (Objects.isNull(contractAddress)) {
            throw new Exception("Failed to deploy proxy contract.");
        }

        BfsServiceWrapper bfsServiceWrapper = new BfsServiceWrapper();
        BFSService bfsService = new BFSService(client, credentials.generateKeyPair());
        RetCode retCode = bfsService.link(linkName, "latest", contractAddress, metadata.abi);

        if (retCode.getCode() < PrecompiledRetCode.CODE_SUCCESS.getCode()) {
            throw new RuntimeException(" registerCns failed, error message: " + retCode);
        }

        BFSInfo bfsInfo = new BFSInfo("latest", "link");
        bfsInfo.setAddress(contractAddress);
        bfsInfo.setAbi(metadata.abi);
        return bfsInfo;
    }

    public void deploy() throws Exception {
        if (!connection.hasHubDeployed()) {
            System.out.println("Deploy WeCrossHub to chain " + chainPath + " ...");

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            File file = resolver.getResource("classpath:" + hubContractFile).getFile();
            deployContractAndLinkBFS(file, BCOSConstant.BCOS_HUB_NAME, BCOSConstant.BCOS_HUB_NAME);
            System.out.println(
                    "SUCCESS: WeCrossHub: /apps/WeCrossHub/latest has been deployed! chain: "
                            + chainPath);
        } else {
            System.out.println(
                    "SUCCESS: WeCrossHub has already been deployed! chain: " + chainPath);
        }
    }

    public void upgrade() throws Exception {

        System.out.println("Upgrade WeCrossHub to chain " + chainPath + " ...");

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        File file = resolver.getResource("classpath:" + hubContractFile).getFile();
        deployContractAndLinkBFS(file, BCOSConstant.BCOS_HUB_NAME, BCOSConstant.BCOS_HUB_NAME);

        System.out.println(
                "SUCCESS: WeCrossHub: /apps/WeCrossHub/latest has been upgraded! chain: "
                        + chainPath);
    }

    public void getHubAddress() {
        try {
            if (!connection.hasHubDeployed()) {
                System.out.println("WeCrossHub has not been deployed");
            } else {
                System.out.println("WeCrossHub address: " + connection.getHubAddress());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
