package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSRequestType;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.LRUCache;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
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
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class AsyncCnsService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncCnsService.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private final long CNS_EXPIRES = 20000; // millseconds

    class CnsInfoExt {
        public CnsInfo getCnsInfo() {
            return cnsInfo;
        }

        private CnsInfo cnsInfo;
        private long expires;

        public CnsInfoExt(CnsInfo cnsInfo) {
            this.cnsInfo = cnsInfo;
            this.expires = System.currentTimeMillis() + CNS_EXPIRES;
        }

        public boolean isAvailable() {
            return this.expires > System.currentTimeMillis();
        }
    }

    private LRUCache<String, CnsInfoExt> abiCache = new LRUCache<>(32);
    private LRUCache<String, CnsInfoExt> address2CnsInfoCache = new LRUCache<>(128);
    private ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1, new CustomizableThreadFactory("AsyncCnsService-"));
    private static final long CLEAR_EXPIRES = 30L * 60L; // 30 min
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
                () -> abiCache.clear(), CLEAR_EXPIRES, CLEAR_EXPIRES, TimeUnit.SECONDS);
    }

    public interface QueryCallback {
        void onResponse(Exception e, String abi, String address);
    }

    public void queryABI(
            String name, Driver driver, Connection connection, QueryCallback callback) {
        try {

            /** WeCrossProxy ABI */
            if (BCOSConstant.BCOS_PROXY_NAME.equals(name)) {
                String proxyABI = connection.getProperties().get(BCOSConstant.BCOS_PROXY_ABI);
                String proxyAddress = connection.getProperties().get(BCOSConstant.BCOS_PROXY_NAME);
                if (logger.isTraceEnabled()) {
                    logger.trace("ProxyABI: {}", proxyABI);
                }
                callback.onResponse(null, proxyABI, proxyAddress);
                return;
            }

            CnsInfoExt cnsInfoExt = abiCache.get(name);
            if (cnsInfoExt != null && cnsInfoExt.isAvailable()) {
                callback.onResponse(
                        null,
                        cnsInfoExt.getCnsInfo().getAbi(),
                        cnsInfoExt.getCnsInfo().getAddress());
                return;
            }

            queryABISemaphore.acquire(1); // Only 1 thread can query abi remote
            cnsInfoExt = abiCache.get(name);
            if (cnsInfoExt != null && cnsInfoExt.isAvailable()) {
                queryABISemaphore.release();
                callback.onResponse(
                        null,
                        cnsInfoExt.getCnsInfo().getAbi(),
                        cnsInfoExt.getCnsInfo().getAddress());
                return;
            }

            selectByName(
                    name,
                    connection,
                    (exception, infoList) -> {
                        queryABISemaphore.release();

                        if (Objects.nonNull(exception)) {
                            callback.onResponse(exception, null, null);
                            return;
                        }

                        if (Objects.isNull(infoList) || infoList.isEmpty()) {
                            callback.onResponse(null, null, null);
                        } else {
                            int size = infoList.size();
                            CnsInfo currentCnsInfo = infoList.get(size - 1);

                            addAbiToCache(name, currentCnsInfo);

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "queryABI name:{}, abi:{}, address:{}",
                                        name,
                                        currentCnsInfo.getAbi(),
                                        currentCnsInfo.getAddress());
                            }

                            callback.onResponse(
                                    null, currentCnsInfo.getAbi(), currentCnsInfo.getAddress());
                        }
                    });
        } catch (Exception e) {
            queryABISemaphore.release();
            callback.onResponse(e, null, null);
        }
    }

    public interface SelectCallback {
        void onResponse(Exception e, List<CnsInfo> infoList);
    }

    public void selectByName(String name, Connection connection, SelectCallback callback) {
        select(name, connection, callback);
    }

    private void select(String name, Connection connection, SelectCallback callback) {
        Request request = new Request();
        request.setType(BCOSRequestType.QUERY_CNS);
        request.setData(name.getBytes(StandardCharsets.UTF_8));
        connection.asyncSend(
                request,
                new Connection.Callback() {
                    @Override
                    public void onResponse(Response response) {
                        try {
                            if (response.getErrorCode() != 0) {
                                throw new Exception(
                                        "Select response error: " + response.getErrorMessage());
                            }

                            CnsInfo cnsInfo =
                                    objectMapper.readValue(
                                            response.getData(), new TypeReference<CnsInfo>() {});

                            if (cnsInfo == null) {
                                throw new Exception("Abi of \"" + name + "\" not found.");
                            }

                            List<CnsInfo> cnsInfos = new LinkedList<>();
                            cnsInfos.add(cnsInfo);
                            callback.onResponse(null, cnsInfos);
                        } catch (Exception e) {
                            logger.error("select exception: ", e);
                            callback.onResponse(e, null);
                        }
                    }
                });
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
        path.setResource(BCOSConstant.BCOS_CNS_NAME);

        TransactionContext transactionContext = new TransactionContext(null, path, null, null);

        driver.asyncCall(
                transactionContext,
                transactionRequest,
                false,
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
                                    new Exception(connectionResponse.getMessage()), null);
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
            Path path,
            String address,
            String version,
            String abi,
            Account account,
            BlockManager blockManager,
            Connection connection,
            InsertCallback callback) {

        Path proxyPath = new Path();
        proxyPath.setResource(BCOSConstant.BCOS_PROXY_NAME);

        TransactionRequest transactionRequest =
                new TransactionRequest(
                        BCOSConstant.PPROXY_METHOD_REGISTER,
                        Arrays.asList(path.toString(), version, address, abi)
                                .toArray(new String[0]));

        TransactionContext requestTransactionContext =
                new TransactionContext(account, proxyPath, null, blockManager);

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
                                res.getMessage());
                        callback.onResponse(new Exception(res.getMessage()));
                        return;
                    }

                    // addAbiToCache(path.getResource(), abi);

                    logger.info(
                            " registerCNS successfully, name: {}, version: {}, address: {} ",
                            path.getResource(),
                            version,
                            address);

                    callback.onResponse(null);
                });
    }

    public LRUCache<String, CnsInfoExt> getAbiCache() {
        return abiCache;
    }

    public void addAbiToCache(String name, CnsInfo cnsInfo) {
        CnsInfoExt cnsInfoExt = new CnsInfoExt(cnsInfo);
        this.abiCache.put(name, cnsInfoExt);
        this.address2CnsInfoCache.put(cnsInfo.getAddress(), cnsInfoExt);
    }

    public String getNameByAddressFromCache(String address) {
        CnsInfoExt cnsInfoExt = address2CnsInfoCache.get(address);

        if (cnsInfoExt == null) {
            return null;
        }
        return cnsInfoExt.getCnsInfo().getName();
    }
}
