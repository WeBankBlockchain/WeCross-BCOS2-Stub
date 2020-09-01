package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.LRUCache;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncCnsService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncCnsService.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private LRUCache<String, String> abiCache = new LRUCache<>(32);
    private ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1024);
    private static final long CLEAR_EXPIRES = 30 * 60; // 30 min
    private Semaphore queryABISemaphore = new Semaphore(1, true);

    private BCOSDriver bcosDriver = null;

    public BCOSDriver getBcosDriver() {
        return bcosDriver;
    }

    public void setBcosDriver(BCOSDriver bcosDriver) {
        this.bcosDriver = bcosDriver;
    }

    public AsyncCnsService() {
        this.scheduledExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        abiCache.clear();
                    }
                },
                CLEAR_EXPIRES,
                CLEAR_EXPIRES,
                TimeUnit.SECONDS);
    }

    public interface QueryCallback {
        void onResponse(Exception e, String info);
    }

    public void queryABI(
            String name, Driver driver, Connection connection, QueryCallback callback) {
        try {

            /** WeCrossProxy ABI */
            if (BCOSConstant.BCOS_PROXY_NAME.equals(name)) {
                String proxyABI = driver.getProperties(connection).get(BCOSConstant.BCOS_PROXY_ABI);
                if (logger.isTraceEnabled()) {
                    logger.trace("ProxyABI: {}", proxyABI);
                }
                callback.onResponse(null, proxyABI);
                return;
            }

            String abi = abiCache.get(name);
            if (abi != null) {
                callback.onResponse(null, abi);
                return;
            }

            queryABISemaphore.acquire(1); // Only 1 thread can query abi remote
            abi = abiCache.get(name);
            if (abi != null) {
                queryABISemaphore.release();
                callback.onResponse(null, abi);
                return;
            }

            selectByName(
                    name,
                    connection,
                    driver,
                    (exception, infoList) -> {
                        queryABISemaphore.release();

                        if (Objects.nonNull(exception)) {
                            callback.onResponse(exception, null);
                            return;
                        }

                        if (Objects.isNull(infoList) || infoList.isEmpty()) {
                            callback.onResponse(null, null);
                        } else {
                            int size = infoList.size();
                            String currentAbi = infoList.get(size - 1).getAbi();

                            addAbiToCache(name, currentAbi);

                            if (logger.isDebugEnabled()) {
                                logger.debug("queryABI name:{}, abi:{}", name, currentAbi);
                            }

                            callback.onResponse(null, currentAbi);
                        }
                    });
        } catch (Exception e) {
            queryABISemaphore.release();
            callback.onResponse(e, null);
        }
    }

    public interface SelectCallback {
        void onResponse(Exception e, List<CnsInfo> infoList);
    }

    public void selectByNameAndVersion(
            String name,
            String version,
            Connection connection,
            Driver driver,
            SelectCallback callback) {
        select(name, version, connection, driver, callback);
    }

    public void selectByName(
            String name, Connection connection, Driver driver, SelectCallback callback) {
        select(name, null, connection, driver, callback);
    }

    private void select(
            String name,
            String version,
            Connection connection,
            Driver driver,
            SelectCallback callback) {

        TransactionRequest transactionRequest = new TransactionRequest();
        if (Objects.isNull(version)) {
            transactionRequest.setArgs(new String[] {name});
            transactionRequest.setMethod("selectByName");
        } else {
            transactionRequest.setArgs(new String[] {name});
            transactionRequest.setMethod("selectByNameAndVersion");
        }

        Path path = new Path();
        path.setResource(BCOSConstant.BCOS_PROXY_NAME);

        TransactionContext transactionContext = new TransactionContext(null, path, null, null);

        driver.asyncCall(
                transactionContext,
                transactionRequest,
                true,
                connection,
                (transactionException, connectionResponse) -> {
                    try {
                        if (Objects.nonNull(transactionException)) {
                            callback.onResponse(
                                    new Exception(transactionException.getMessage()), null);
                            return;
                        }

                        if (connectionResponse.getErrorCode() != BCOSStatusCode.Success) {
                            callback.onResponse(
                                    new Exception(connectionResponse.getErrorMessage()), null);
                            return;
                        }

                        List<CnsInfo> infoList =
                                objectMapper.readValue(
                                        connectionResponse.getResult()[0],
                                        objectMapper
                                                .getTypeFactory()
                                                .constructCollectionType(
                                                        List.class, CnsInfo.class));
                        callback.onResponse(null, infoList);

                    } catch (Exception e) {
                        logger.warn("exception occurs", e);
                        callback.onResponse(new Exception(e.getMessage()), null);
                    }
                });
    }

    public interface InsertCallback {
        void onResponse(Exception e);
    }

    public void registerCNSByProxy(
            String name,
            String address,
            String version,
            String abi,
            Account account,
            BlockHeaderManager blockHeaderManager,
            Connection connection,
            InsertCallback callback) {

        Path path = new Path();
        path.setResource(BCOSConstant.BCOS_PROXY_NAME);

        TransactionRequest transactionRequest =
                new TransactionRequest(
                        "registerCNS",
                        Arrays.asList(name, version, address, abi).toArray(new String[0]));

        TransactionContext requestTransactionContext =
                new TransactionContext(account, path, null, blockHeaderManager);

        bcosDriver.asyncSendTransaction(
                requestTransactionContext,
                transactionRequest,
                true,
                connection,
                (exception, res) -> {
                    if (Objects.nonNull(exception)) {
                        logger.error(" registerCNS e: ", exception);
                        callback.onResponse(exception);
                        return;
                    }

                    if (res.getErrorCode() != BCOSStatusCode.Success) {
                        logger.error(
                                " deployAndRegisterCNS, error: {}, message: {}",
                                res.getErrorCode(),
                                res.getErrorMessage());
                        callback.onResponse(new Exception(res.getErrorMessage()));
                        return;
                    }

                    addAbiToCache(name, abi);

                    logger.info(
                            " registerCNS successfully, name: {}, version: {}, address: {} ",
                            name,
                            version,
                            address);

                    callback.onResponse(null);
                });
    }

    public LRUCache<String, String> getAbiCache() {
        return abiCache;
    }

    public void setAbiCache(LRUCache<String, String> abiCache) {
        this.abiCache = abiCache;
    }

    public void addAbiToCache(String name, String abi) {
        // logger.info(" add abi cache, name: {}, abi: {}", name, abi);
        this.abiCache.put(name, abi);
    }
}
