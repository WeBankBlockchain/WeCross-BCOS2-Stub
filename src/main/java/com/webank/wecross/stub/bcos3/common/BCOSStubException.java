package com.webank.wecross.stub.bcos3.common;

import com.webank.wecross.exception.WeCrossException;

public class BCOSStubException extends WeCrossException {
    public BCOSStubException(Integer errorCode, String message) {
        super(errorCode, message);
    }
}
