package com.webank.wecross.stub.bcos.performance;

public interface PerformanceSuite {
    String getName();

    void call(PerformanceSuiteCallback callback, int index);
}
