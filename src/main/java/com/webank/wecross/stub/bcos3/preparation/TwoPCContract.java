package com.webank.wecross.stub.bcos3.preparation;

import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.bcos3.BCOSConnection;
import com.webank.wecross.stub.bcos3.account.BCOSAccount;
import com.webank.wecross.stub.bcos3.client.ClientBlockManager;
import com.webank.wecross.stub.bcos3.custom.DeployContractHandler;
import com.webank.wecross.stub.bcos3.custom.DeployContractWasmHandler;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwoPCContract {

    private static final Logger logger = LoggerFactory.getLogger(TwoPCContract.class);

    public TwoPCContract(BCOSAccount account, BCOSConnection connection) {
        this.account = account;
        this.connection = connection;
        this.blockManager = new ClientBlockManager(connection.getClientWrapper());
    }

    private BCOSAccount account;
    private BCOSConnection connection;
    private BlockManager blockManager;
    private DeployContractHandler deployContractHandler;
    private DeployContractWasmHandler deployContractWasmHandler;

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public void setBlockManager(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    public DeployContractHandler getDeployContractHandler() {
        return deployContractHandler;
    }

    public void setDeployContractHandler(DeployContractHandler deployContractHandler) {
        this.deployContractHandler = deployContractHandler;
    }

    public DeployContractWasmHandler getDeployContractWasmHandler() {
        return deployContractWasmHandler;
    }

    public void setDeployContractWasmHandler(DeployContractWasmHandler deployContractWasmHandler) {
        this.deployContractWasmHandler = deployContractWasmHandler;
    }

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

    public void deploy2PCContract(
            String contractName,
            String version,
            String contractContent,
            int tps,
            int from,
            int to,
            CryptoSuite cryptoSuite,
            boolean isWasm)
            throws Exception {

        logger.info(
                "contract: {}, version: {}, from: {}, to: {}, tps: {}",
                contractName,
                version,
                from,
                to,
                tps);

        Semaphore semaphore = new Semaphore(to - from + 1, true);

        for (int index = from; index < to; index++) {

            semaphore.acquire(1);

            String resource = contractName + "_" + index;

            Object[] params = new Object[] {resource, contractContent, contractName, version};

            Thread.sleep(1000 / tps);
            if (isWasm) {
                deployContractWasmHandler.handle(
                        null,
                        params,
                        account,
                        blockManager,
                        connection,
                        (Driver.CustomCommandCallback)
                                (error, response) -> {
                                    semaphore.release(1);
                                    if (Objects.nonNull(error)) {
                                        System.err.println(
                                                " Unable deploy resource: "
                                                        + resource
                                                        + " ,e: "
                                                        + error.getMessage());
                                    } else {
                                        System.err.println(
                                                " Deploy resource: "
                                                        + resource
                                                        + " successfully, address: "
                                                        + (String) response);
                                    }
                                },
                        cryptoSuite);
            } else {
                deployContractHandler.handle(
                        null,
                        params,
                        account,
                        blockManager,
                        connection,
                        (Driver.CustomCommandCallback)
                                (error, response) -> {
                                    semaphore.release(1);
                                    if (Objects.nonNull(error)) {
                                        System.err.println(
                                                " Unable deploy resource: "
                                                        + resource
                                                        + " ,e: "
                                                        + error.getMessage());
                                    } else {
                                        System.err.println(
                                                " Deploy resource: "
                                                        + resource
                                                        + " successfully, address: "
                                                        + (String) response);
                                    }
                                },
                        cryptoSuite);
            }
        }

        semaphore.acquire(to - from + 1);
    }
}
