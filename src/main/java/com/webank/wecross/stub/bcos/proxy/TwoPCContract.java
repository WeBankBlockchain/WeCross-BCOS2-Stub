package com.webank.wecross.stub.bcos.proxy;

import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.custom.DeployContractHandler;
import com.webank.wecross.stub.bcos.web3j.DefaultBlockHeaderManager;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwoPCContract {

    private static final Logger logger = LoggerFactory.getLogger(TwoPCContract.class);

    public TwoPCContract(BCOSAccount account, BCOSConnection connection) {
        this.account = account;
        this.connection = connection;
        this.blockHeaderManager = new DefaultBlockHeaderManager(connection.getWeb3jWrapper());
    }

    private BCOSAccount account;
    private BCOSConnection connection;
    private BlockHeaderManager blockHeaderManager;
    private DeployContractHandler deployContractHandler;

    public BlockHeaderManager getBlockHeaderManager() {
        return blockHeaderManager;
    }

    public void setBlockHeaderManager(BlockHeaderManager blockHeaderManager) {
        this.blockHeaderManager = blockHeaderManager;
    }

    public DeployContractHandler getDeployContractHandler() {
        return deployContractHandler;
    }

    public void setDeployContractHandler(DeployContractHandler deployContractHandler) {
        this.deployContractHandler = deployContractHandler;
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
            String contractName, String version, String contractContent, int tps, int from, int to)
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

            deployContractHandler.handle(
                    null,
                    params,
                    account,
                    blockHeaderManager,
                    connection,
                    new Driver.CustomCommandCallback() {
                        @Override
                        public void onResponse(Exception error, Object response) {
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
                        }
                    });
        }

        semaphore.acquire(to - from + 1);
    }
}
