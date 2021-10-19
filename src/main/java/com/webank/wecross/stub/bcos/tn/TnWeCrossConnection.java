package com.webank.wecross.stub.bcos.tn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TnWeCrossConnection implements Connection {
    private static Logger logger = LoggerFactory.getLogger(TnWeCrossConnection.class);
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private org.trustnet.protocol.link.Connection tnConnection;
    private Map<String, String> properties;
    private String verifierString = null;

    public TnWeCrossConnection(org.trustnet.protocol.link.Connection tnConnection) {
        this.tnConnection = tnConnection;
    }

    @Override
    public void asyncSend(Request request, Callback callback) {
        tnConnection.asyncSend(
                request.getPath(),
                request.getType(),
                request.getData(),
                new org.trustnet.protocol.link.Connection.Callback() {
                    @Override
                    public void onResponse(int errorCode, String message, byte[] responseData) {
                        Response response = new Response();
                        response.setErrorCode(errorCode);
                        response.setErrorMessage(message);
                        response.setData(responseData);
                        callback.onResponse(response);
                    }
                });
    }

    @Override
    public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {
        // do nothing, no need to notify resources changing
    }

    @Override
    public Map<String, String> getProperties() {
        if (properties != null) {
            return properties;
        }

        try {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            tnConnection.asyncSend(
                    "",
                    TnDefault.GET_PROPERTIES,
                    new byte[] {},
                    new org.trustnet.protocol.link.Connection.Callback() {
                        @Override
                        public void onResponse(int errorCode, String message, byte[] responseData) {
                            if (errorCode != 0) {
                                logger.warn(
                                        "getProperties failed, status: {}, message: {}",
                                        errorCode,
                                        message);
                                future.complete(null);
                            } else {
                                future.complete(responseData);
                            }
                        }
                    });
            byte[] propertiesBytes = future.get(TnDefault.ADAPTER_QUERY_EXPIRES, TimeUnit.SECONDS);
            Map<String, String> retProperties = new HashMap<>();
            retProperties = objectMapper.readValue(propertiesBytes, retProperties.getClass());
            properties = retProperties;

            // replace verifier to local configure
            properties.put(BCOSConstant.BCOS_SEALER_LIST, verifierString);

            return properties;
        } catch (Exception e) {
            logger.warn("getProperties exception: ", e);
        }

        return null;
    }

    public org.trustnet.protocol.link.Connection getTnConnection() {
        return tnConnection;
    }

    public void setVerifierString(String verifierString) {
        this.verifierString = verifierString;
    }
}
