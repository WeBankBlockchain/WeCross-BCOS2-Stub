package com.webank.wecross.stub.bcos.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos.performance.PerformanceManager;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class PerformanceTest {

    private static Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    public static void usage() {
        System.err.println("Usage:");

        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " [chainName] [accountName] [contractName] call [count] [qps] [enableGM]");
        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " [chainName] [accountName] [contractName] sendTransaction [count] [qps] [enableGM]");
        System.err.println("Example:");
        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " chains/bcos bcos_user1 HelloWeCross call 10000 1000");
        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " chains/bcos bcos_user1 HelloWeCross sendTransaction 10000 1000");

        exit();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 6) {
            usage();
        }

        String chainName = args[0];
        String accountName = args[1];
        String contractName = args[2];
        String command = args[3];
        BigInteger count = new BigInteger(args[4]);
        BigInteger qps = new BigInteger(args[5]);
        boolean sm = false;
        if (args.length > 6) {
            sm = Boolean.valueOf(args[6]);
        }

        System.err.println(
                "BCOSPerformanceTest [HelloWeCross]: "
                        + ", command: "
                        + command
                        + ", chain: "
                        + chainName
                        + ", account: "
                        + accountName
                        + ", contractName: "
                        + contractName
                        + ", count: "
                        + count
                        + ", qps: "
                        + qps
                        + ", enableGM: "
                        + sm);

        try {
            PureBCOSProxySuite suite =
                    command.equals("sendTransaction")
                            ? new PureBCOSProxySendTransactionSuite(
                                    contractName, chainName, accountName, sm)
                            : new PureBCOSProxyCallSuite(
                                    contractName, chainName, accountName, sm);
            CnsInfo cnsInfo = suite.getCnsInfo();
            if (Objects.isNull(cnsInfo)) {
                System.err.println(" ## Error: unable to fetch proxy contract address. ");
                System.exit(0);
            }
            System.err.println(
                    " ## Proxy contract address: "
                            + cnsInfo.getAddress()
                            + " ,version: "
                            + cnsInfo.getVersion());

            PerformanceManager performanceManager = new PerformanceManager(suite, count, qps);
            performanceManager.run();
        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println("Error: " + e + " please check logs/error.log");
            exit();
        }
    }

    private static void exit() {
        System.exit(0);
    }
}
