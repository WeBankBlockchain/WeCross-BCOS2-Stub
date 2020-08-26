package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.WeCrossContext;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stub("GM_BCOS2.0")
public class BCOSGMStubFactory extends BCOSBaseStubFactory {
    private Logger logger = LoggerFactory.getLogger(BCOSGMStubFactory.class);

    public BCOSGMStubFactory() {
        EncryptType encryptType = new EncryptType(EncryptType.SM2_TYPE);
        logger.info(" EncryptType: {}", encryptType.getEncryptType());
    }

    /**
     * The algorithm name, secp256k1 or sm2p256v1
     *
     * @return
     */
    @Override
    public String getAlg() {
        return "sm2p256v1";
    }

    /**
     * The stub type, BCOS2.0 or GM_BCOS2.0
     *
     * @return
     */
    @Override
    public String getStubType() {
        Class<?> stubFactoryClass = BCOSGMStubFactory.class;
        Stub annotation = stubFactoryClass.getAnnotation(Stub.class);
        if (logger.isDebugEnabled()) {
            logger.debug(" stub type: {}", annotation.value());
        }
        return annotation.value();
    }

    public static void main(String[] args) throws Exception {
        System.out.println(
                "This is BCOS2.0 Guomi Stub Plugin. Please copy this file to router/plugin/");
        System.out.println("For deploy proxy contract:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.proxy.ProxyContractDeployment");
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
