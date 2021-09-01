package com.webank.wecross.stub.bcos.integration;

import com.moandjiezana.toml.Toml;
import link.luyu.protocol.network.Events;
import org.junit.Test;
import link.luyu.protocol.link.Connection;
import link.luyu.protocol.link.Driver;
import link.luyu.protocol.link.PluginBuilder;
import link.luyu.protocol.link.bcos.LuyuBCOSPluginBuilder;
import link.luyu.protocol.network.Account;
import link.luyu.protocol.network.AccountManager;
import link.luyu.protocol.network.Block;
import link.luyu.protocol.network.CallRequest;
import link.luyu.protocol.network.CallResponse;
import link.luyu.protocol.network.Receipt;
import link.luyu.protocol.network.Transaction;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LuyuTest {
    Connection connection;
    Driver driver;
    AccountManager accountManager;

    String userSecKey = "-----BEGIN PRIVATE KEY-----\n" +
            "MIGNAgEAMBAGByqGSM49AgEGBSuBBAAKBHYwdAIBAQQgZ0YKGKLaJAWCWvY2ulaa\n" +
            "3coGNL6pjXLl8XiWWb4315+gBwYFK4EEAAqhRANCAARFwpYim0j+vnxZEFfX4uq5\n" +
            "VnE8fYK6zOQwqsnUB9mL83jY2IvN2+cwWRukEYqsxU2kbtuupmVxkXXdzl+YbUA2\n" +
            "-----END PRIVATE KEY-----";

    public LuyuTest() throws Exception {
        Map<String, Object> driverConfig = getToml("classpath:chains/bcos/driver.toml").toMap();
        Map<String, Object> connectionConfig = getToml("classpath:chains/bcos/connection.toml").toMap();

        driverConfig.put("chainPath", "payment.bcos");
        driverConfig.put("chainDir", "chains/bcos");

        connectionConfig.put("chainPath", "payment.bcos");
        connectionConfig.put("chainDir", "chains/bcos");

        PluginBuilder builder = new LuyuBCOSPluginBuilder();
        connection = builder.newConnection(connectionConfig);
        driver = builder.newDriver(connection, driverConfig);
        accountManager = new MockAccountManager();

        driver.start();
    }

    @Test
    public void chainEventTest() throws Exception {

        driver.registerEvents(new Events() {
            @Override
            public void sendTransaction(Account account, Transaction tx, Driver.ReceiptCallback callback) {
                driver.sendTransaction(account, tx, callback);
            }

            @Override
            public void call(Account account, CallRequest request, Driver.CallResponseCallback callback) {
                driver.call(account, request, callback);
            }

            @Override
            public void getAccountByIdentity(String identity, KeyCallback callback) {
                callback.onResponse(accountManager.getAccountByIdentity(driver.getSignatureType(), identity));
            }
        });

        for (int i = 0; i < 10000; i++) {
            Thread.sleep(1000);
        }
    }

    @Test
    public void sendTransactionTest() throws Exception {
        Transaction request = new Transaction();
        request.setPath("payment.bcos.HelloWorld");
        request.setMethod("set");
        request.setArgs(new String[]{"aaa"});

        Account account = accountManager.getAccountBySignature(driver.getSignatureType(), "SignBytes".getBytes(StandardCharsets.UTF_8), null);

        CompletableFuture<Receipt> future = new CompletableFuture<>();
        driver.sendTransaction(account, request, new Driver.ReceiptCallback() {
            @Override
            public void onResponse(int status, String message, Receipt receipt) {
                future.complete(receipt);
            }
        });
        future.get();
    }

    @Test
    public void callTest() throws Exception {
        CallRequest request = new CallRequest();
        request.setPath("payment.bcos.HelloWorld");
        request.setMethod("get");
        request.setArgs(new String[]{});

        Account account = accountManager.getAccountBySignature(driver.getSignatureType(), "SignBytes".getBytes(StandardCharsets.UTF_8), null);

        CompletableFuture<CallResponse> future = new CompletableFuture<>();
        driver.call(account, request, new Driver.CallResponseCallback() {
            @Override
            public void onResponse(int status, String message, CallResponse callResponse) {
                future.complete(callResponse);
            }
        });
        future.get();
    }

    @Test
    public void listResourceTest() throws Exception {
        CompletableFuture<link.luyu.protocol.network.Resource[]> future = new CompletableFuture<>();
        driver.listResources(new Driver.ResourcesCallback() {
            @Override
            public void onResponse(int status, String message, link.luyu.protocol.network.Resource[] resources) {
                future.complete(resources);
            }
        });
        future.get();
    }

    @Test
    public void getBlockNumberTest() throws Exception {
        long number = driver.getBlockNumber();
        System.out.println(number);
    }

    @Test
    public void getBlockTest() throws Exception {
        CompletableFuture<Block> future = new CompletableFuture<>();
        driver.getBlockByNumber(0, new Driver.BlockCallback() {
            @Override
            public void onResponse(int status, String message, Block block) {
                future.complete(block);
            }
        });
        future.get();
    }

    private Toml getToml(String path) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource(path);
        return new Toml().read(resource.getInputStream());
    }

    @Test
    public void test() {
        String key = new String("0x5c99abda2c754ce3b363d34cba5831a619a4de48b908e25d1d6a9fac4a0e90e0");
        System.out.println(Arrays.toString(key.getBytes(StandardCharsets.UTF_8)));
    }
}
