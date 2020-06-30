package com.webank.wecross.stub.bcos.proxy;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSConnectionFactory;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.common.BCOSFileUtils;
import com.webank.wecross.stub.bcos.custom.CommandHandler;
import com.webank.wecross.stub.bcos.custom.DeployContractHandler;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ProxyContract {
    private String proxyContractDir;
    private String chainPath;
    private String accountName;

    private Account account;
    private BCOSConnection connection;
    private Driver driver;
    private BlockHeaderManager blockHeaderManager;

    public ProxyContract(String proxyContractDir, String chainPath, String accountName)
            throws Exception {
        this.proxyContractDir = proxyContractDir;
        this.chainPath = chainPath;
        this.accountName = accountName;

        BCOSStubFactory bcosStubFactory = new BCOSStubFactory();
        account =
                bcosStubFactory.newAccount(
                        accountName, "classpath:accounts" + File.separator + accountName);
        driver = bcosStubFactory.newDriver();
        connection = BCOSConnectionFactory.build(chainPath, "stub.toml", null);
        blockHeaderManager = new DirectBlockHeaderManager(driver, connection);
    }

    public void deploy() throws Exception {
        if (!connection.hasProxyDeployed()) {
            System.out.println("Deploy WeCrossProxy to chain " + chainPath + " ...");

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            String path = resolver.getResource(proxyContractDir).getFile().getAbsolutePath();
            String zipName = BCOSFileUtils.zipDir(path);

            File file = new File(zipName);
            byte[] contractBytes;
            try {
                contractBytes = Files.readAllBytes(file.toPath());
            } finally {
                file.delete();
            }

            Object[] args =
                    new Object[] {
                        Base64.getEncoder().encodeToString(contractBytes),
                        String.valueOf(System.currentTimeMillis())
                    };

            CompletableFuture<Map.Entry<Exception, Object>> future = new CompletableFuture<>();

            CommandHandler commandHandler = new DeployContractHandler();
            commandHandler.handle(
                    Path.decode("a.b.WeCrossProxy"),
                    args,
                    account,
                    blockHeaderManager,
                    connection,
                    new HashMap<>(),
                    (error, response) -> {
                        future.complete(
                                new HashMap.SimpleEntry<Exception, Object>(error, response));
                    });

            Map.Entry<Exception, Object> deployReturn = future.get(10, TimeUnit.SECONDS);
            Exception error = deployReturn.getKey();
            if (Objects.nonNull(error)) {
                throw error;
            }
        }

        System.out.println("SUCCESS: proxy has been deployed! chain: " + chainPath);
    }

    public static void check(String chainPath) {
        try {
            BCOSConnection connection = BCOSConnectionFactory.build(chainPath, "stub.toml", null);

            if (!connection.hasProxyDeployed()) {
                System.out.println("WeCrossProxy has not been deployed");
            } else {
                System.out.println("WeCrossProxy has been deployed.");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static class DirectBlockHeaderManager implements BlockHeaderManager {
        private Driver driver;
        private Connection connection;

        public DirectBlockHeaderManager(Driver driver, Connection connection) {
            this.driver = driver;
            this.connection = connection;
        }

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
            driver.asyncGetBlockNumber(
                    connection,
                    new Driver.GetBlockNumberCallback() {
                        @Override
                        public void onResponse(Exception e, long blockNumber) {
                            callback.onResponse(e, blockNumber);
                        }
                    });
        }

        @Override
        public void asyncGetBlockHeader(long blockNumber, GetBlockHeaderCallback callback) {
            driver.asyncGetBlockHeader(
                    blockNumber,
                    connection,
                    new Driver.GetBlockHeaderCallback() {
                        @Override
                        public void onResponse(Exception e, byte[] blockHeader) {
                            callback.onResponse(e, blockHeader);
                        }
                    });
        }
    }
}
