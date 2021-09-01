package com.webank.wecross.stub.bcos.luyu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.web3j.AbstractWeb3jWrapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import link.luyu.protocol.link.Connection;
import link.luyu.protocol.network.Resource;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuyuConnectionAdapter implements Connection {
    private static Logger logger = LoggerFactory.getLogger(LuyuConnectionAdapter.class);
    private com.webank.wecross.stub.Connection wecrossConnection;
    private Path chainPath;
    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private Map<String, Resource> resources = new HashMap<>();
    private ChainEventManager chainEventManager;

    public LuyuConnectionAdapter(
            com.webank.wecross.stub.Connection wecrossConnection, String chainPathStr)
            throws Exception {
        this.wecrossConnection = wecrossConnection;
        this.chainPath = Path.decode(chainPathStr);

        if (wecrossConnection instanceof BCOSConnection) {
            logger.info("Enable chain event manager.");
            AbstractWeb3jWrapper web3jWrapper =
                    ((BCOSConnection) wecrossConnection).getWeb3jWrapper();
            chainEventManager = new ChainEventManager(web3jWrapper);
        }
    }

    @Override
    public void start() throws RuntimeException {
        List<ResourceInfo> currentResources = ((BCOSConnection) wecrossConnection).getResources();
        updateResourceBackup(currentResources);
        this.wecrossConnection.setConnectionEventHandler(
                new com.webank.wecross.stub.Connection.ConnectionEventHandler() {
                    @Override
                    public void onResourcesChange(List<ResourceInfo> resourceInfos) {
                        updateResourceBackup(resourceInfos);
                    }
                });
    }

    @Override
    public void stop() throws RuntimeException {}

    private void updateResourceBackup(List<ResourceInfo> resourceInfos) {
        if (resourceInfos == null) {
            return;
        }
        for (ResourceInfo info : resourceInfos) {
            // assume that there is no resource deletion in this chain
            Resource resource = resources.get(info.getName());
            if (resource == null) {
                // build path
                Path path = new Path();
                path.setZone(chainPath.getZone());
                path.setChain(chainPath.getChain());
                path.setResource(info.getName());

                // build Resource
                resource = new Resource();
                resource.setType(info.getStubType());
                resource.setPath(path.toString());

                addResource(resource);
            }
            Map<String, Object> properties = resource.getProperties();
            if (properties == null) {
                properties = new HashMap<>();
                resource.setProperties(properties);
            }

            for (Map.Entry<Object, Object> entry : info.getProperties().entrySet()) {
                properties.put((String) entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void asyncSend(String path, int type, byte[] data, Callback callback) {

        if (type == LuyuDefault.GET_PROPERTIES) {
            handleGetProperties(path, type, data, callback);
        } else if (type == LuyuDefault.LIST_RESOURCES) {
            handleListResources(path, type, data, callback);
        } else {
            handleNormalSend(path, type, data, callback);
        }
    }

    @Override
    public void subscribe(int type, byte[] data, Callback callback) {
        if (type == LuyuDefault.SUBSCRIBE_CHAIN_SEND_TX_EVENT) {
            if (chainEventManager != null) {
                chainEventManager.addSendTransactionEventCallback(
                        new ChainEventManager.ChainEventCallback() {
                            @Override
                            public void onEvent(String resourceName, byte[] bytes) {
                                callback.onResponse(0, resourceName, bytes);
                            }
                        });
            }
        } else if (type == LuyuDefault.SUBSCRIBE_CHAIN_CALL_EVENT) {
            if (chainEventManager != null) {
                chainEventManager.addCallEventCallback(
                        new ChainEventManager.ChainEventCallback() {
                            @Override
                            public void onEvent(String resourceName, byte[] bytes) {
                                callback.onResponse(0, resourceName, bytes);
                            }
                        });
            }
        }
    }

    private void handleNormalSend(String path, int type, byte[] data, Callback callback) {
        Request request = new Request();
        request.setPath(path);
        request.setType(type);
        request.setData(data);
        wecrossConnection.asyncSend(
                request,
                new com.webank.wecross.stub.Connection.Callback() {
                    @Override
                    public void onResponse(Response response) {
                        callback.onResponse(
                                response.getErrorCode(),
                                response.getErrorMessage(),
                                response.getData());
                    }
                });
    }

    private void handleGetProperties(String path, int type, byte[] data, Callback callback) {
        try {
            Map<String, String> properties = wecrossConnection.getProperties();
            byte[] propertiesBytes = objectMapper.writeValueAsBytes(properties);

            executor.submit(
                    () -> {
                        callback.onResponse(BCOSStatusCode.Success, "success", propertiesBytes);
                    });
        } catch (Exception e) {
            executor.submit(
                    () -> {
                        callback.onResponse(
                                BCOSStatusCode.HandleGetPropertiesFailed,
                                e.getMessage(),
                                new byte[] {});
                    });
        }
    }

    private void handleListResources(String path, int type, byte[] data, Callback callback) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(resources.values());

            executor.submit(
                    () -> {
                        callback.onResponse(BCOSStatusCode.Success, "success", bytes);
                    });
        } catch (Exception e) {
            executor.submit(
                    () -> {
                        callback.onResponse(
                                BCOSStatusCode.ListResourcesFailed, e.getMessage(), new byte[] {});
                    });
        }
    }

    public void addResource(Resource resource) {
        try {
            String name = Path.decode(resource.getPath()).getResource();
            if (name == null) {
                throw new Exception("Empty resource name of " + resource.toString());
            }
            resources.put(name, resource);
            chainEventManager.registerEvent(resource);
        } catch (Exception e) {
            logger.error("addResource backup exception: ", e);
        }
    }
}
