package com.webank.wecross.stub.bcos3;

import static com.webank.wecross.stub.bcos3.common.BCOSConstant.BCOS3_ECDSA_EVM_STUB_TYPE;

import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import org.fisco.bcos.sdk.v3.model.CryptoType;

@Stub(BCOS3_ECDSA_EVM_STUB_TYPE)
public class BCOS3EcdsaEvmStubFactory extends BCOSBaseStubFactory {

    public BCOS3EcdsaEvmStubFactory() {
        super(CryptoType.ECDSA_TYPE, BCOSConstant.SECP256K1, BCOS3_ECDSA_EVM_STUB_TYPE);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(
                String.format(
                        "This is %s Stub Plugin. Please copy this file to router/plugin/",
                        BCOS3_ECDSA_EVM_STUB_TYPE));
        System.out.println("For deploy WeCrossProxy:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.preparation.ProxyContractDeployment");
        System.out.println("For deploy WeCrossHub:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.preparation.HubContractDeployment");
        System.out.println("For chain performance test, please run the command for more info:");
        System.out.println(
                "    Pure:    java -cp conf/:lib/*:plugin/* "
                        + com.webank.wecross.stub.bcos3.performance.hellowecross.PerformanceTest
                                .class.getName());
        System.out.println(
                "    Proxy:   java -cp conf/:lib/*:plugin/* "
                        + com.webank.wecross.stub.bcos3.performance.hellowecross.proxy
                                .PerformanceTest.class.getName());
    }
}
