package com.webank.wecross.stub.bcos3.performance;

public interface PerformanceSuite {
    String getName();

    void call(PerformanceSuiteCallback callback);
}
