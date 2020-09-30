package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.Stub;
import org.fisco.bcos.web3j.crypto.EncryptType;

@Stub("GM_BCOS2.0")
public class BCOSGMStubFactory extends BCOSBaseStubFactory {

    public BCOSGMStubFactory() {
        super(EncryptType.SM2_TYPE, "sm2p256v1", "GM_BCOS2.0");
    }

    public static void main(String[] args) throws Exception {
        System.out.println(
                "This is BCOS2.0 Guomi Stub Plugin. Please copy this file to router/plugin/");
        System.out.println("For deploy WeCrossProxy:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.preparation.ProxyContractDeployment");
        System.out.println("For deploy WeCrossHub:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.preparation.HubContractDeployment");
        System.out.println("For chain performance test, please run the command for more info:");
        System.out.println(
                "    Pure:    java -cp conf/:lib/*:plugin/* "
                        + com.webank.wecross.stub.bcos.performance.hellowecross.PerformanceTest
                                .class.getName());
        System.out.println(
                "    Proxy:   java -cp conf/:lib/*:plugin/* "
                        + com.webank.wecross.stub.bcos.performance.hellowecross.proxy
                                .PerformanceTest.class.getName());
    }
}
