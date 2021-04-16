package com.webank.wecross.stub.bcos.luyu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.luyu.protocol.link.Connection;

public class LuyuConnectionAdapter implements Connection {
    private com.webank.wecross.stub.Connection wecrossConnection;
    private static ExecutorService executor = Executors.newFixedThreadPool(1);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public LuyuConnectionAdapter(com.webank.wecross.stub.Connection wecrossConnection) {
        this.wecrossConnection = wecrossConnection;
    }

    @Override
    public void asyncSend(String path, int type, byte[] data, Callback callback) {

        if (type == LuyuDefault.GET_PROPERTIES) {
            handleGetProperties(path, type, data, callback);
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
}
