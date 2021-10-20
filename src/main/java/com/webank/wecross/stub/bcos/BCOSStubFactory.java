package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import java.io.File;
import org.fisco.bcos.web3j.crypto.EncryptType;

@Stub("BCOS2.0")
public class BCOSStubFactory extends BCOSBaseStubFactory {

    public BCOSStubFactory() {
        super(EncryptType.ECDSA_TYPE, "secp256k1", BCOSConstant.BCOS_STUB_TYPE);
    }

    public static void help() throws Exception {
        System.out.println("This is BCOS2.0 Stub Plugin. Please copy this file to router/plugin/");
        System.out.println("For account generation:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* "
                        + BCOSStubFactory.class.getName()
                        + " generateAccount <to path(relative path)> <account name>");
        System.out.println(
                "    eg: java -cp conf/:lib/*:plugin/* "
                        + BCOSStubFactory.class.getName()
                        + " generateAccount conf/accounts bcos_account");
        System.out.println("For deploy WeCrossProxy:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.preparation.ProxyContractDeployment");
        System.out.println("For deploy WeCrossHub:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.normal.preparation.HubContractDeployment");
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

    public static void main(String[] args) throws Exception {
        try {
            if (args.length == 3 && args[0].equals("customCommand")) {
                runCustomCommand(args[1], args[2]);
            } else if (args.length == 3 && args[0].equals("generateAccount")) {
                generateAccount(args[1], args[2]);
            } else {
                help();
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(-1);
        }
    }

    public static void runCustomCommand(String accountName, String content) throws Exception {
        BCOSStubFactory factory = new BCOSStubFactory();
        factory.executeCustomCommand(accountName, content);
        System.exit(0);
    }

    public static void generateAccount(String path, String accountName) throws Exception {
        BCOSStubFactory factory = new BCOSStubFactory();
        String accountPath = path + File.separator + accountName;
        factory.generateAccount(accountPath, new String[] {});
    }
}
