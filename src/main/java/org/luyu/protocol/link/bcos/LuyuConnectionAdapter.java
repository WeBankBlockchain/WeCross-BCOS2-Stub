package org.luyu.protocol.link.bcos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.luyu.protocol.link.Connection;
import org.luyu.protocol.network.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuyuConnectionAdapter implements Connection {
    private static Logger logger = LoggerFactory.getLogger(LuyuConnectionAdapter.class);
    private com.webank.wecross.stub.Connection wecrossConnection;
    private Path chainPath;
    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private Map<String, Resource> resources = new HashMap<>();

    public LuyuConnectionAdapter(
            com.webank.wecross.stub.Connection wecrossConnection, String chainPathStr)
            throws Exception {
        this.wecrossConnection = wecrossConnection;
        this.chainPath = Path.decode(chainPathStr);

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

    private void updateResourceBackup(List<ResourceInfo> resourceInfos) {
        if (resourceInfos == null) {
            return;
        }
        for (ResourceInfo info : resourceInfos) {
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

                resources.put(info.getName(), resource);
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
        // no need to use
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
        } catch (Exception e) {
            logger.error("addResource backup exception: ", e);
        }
    }
}
