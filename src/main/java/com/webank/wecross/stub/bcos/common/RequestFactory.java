package com.webank.wecross.stub.bcos.common;

import com.webank.wecross.stub.Request;
import java.nio.charset.StandardCharsets;

public class RequestFactory {
    /**
     * create Request object
     *
     * @param type
     * @param content
     * @return Request
     */
    public static Request requestBuilder(int type, String content) {
        return requestBuilder(type, content.getBytes(StandardCharsets.UTF_8));
    }

    public static Request requestBuilder(int type, byte[] content) {
        Request request = new Request();
        request.setType(type);
        request.setData(content);
        return request;
    }
}
