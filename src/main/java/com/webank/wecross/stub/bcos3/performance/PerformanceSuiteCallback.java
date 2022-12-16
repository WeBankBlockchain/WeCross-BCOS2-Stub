package com.webank.wecross.stub.bcos3.performance;

public interface PerformanceSuiteCallback {
    void onSuccess(String message);

    void onFailed(String message);
}
