package com.webank.wecross.stub.bcos.performance.guomi;

import com.webank.wecross.stub.bcos.performance.PerformanceManager;
import com.webank.wecross.stub.bcos.performance.PerformanceSuite;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceTest {

    private static Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    public static void usage() {
        System.out.println("Usage:");

        System.out.println(
                " \t java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.performance.guomi.PerformanceTest [chainName] [accountName] call [count] [qps]");
        System.out.println(
                " \t java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.performance.guomi.PerformanceTest [chainName] [accountName] sendTransaction [count] [qps]");
        System.out.println("Example:");
        System.out.println(
                " \t java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.performance.guomi.PerformanceTest chains/bcos_gm bcos_gm_user1 call 10000 1000");
        System.out.println(
                " \t java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.performance.guomi.PerformanceTest chains/bcos_gm bcos_gm_user1 sendTransaction 10000 1000");

        exit();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            usage();
        }

        String chainName = args[0];
        String accountName = args[1];
        String command = args[2];
        BigInteger count = new BigInteger(args[3]);
        BigInteger qps = new BigInteger(args[4]);

        System.out.println(
                "BCOSPerformanceTest: command is "
                        + command
                        + ", count is "
                        + count
                        + ", qps is "
                        + qps);

        switch (command) {
            case "call":
                callTest(chainName, accountName, count, qps);
                exit();
            case "sendTransaction":
                sendTransactionTest(chainName, accountName, count, qps);
                exit();
            default:
                usage();
        }
    }

    public static void callTest(
            String chainName, String accountName, BigInteger count, BigInteger qps) {
        try {
            PerformanceSuite suite = new PureBCOSCallSuite(chainName, accountName);
            PerformanceManager performanceManager = new PerformanceManager(suite, count, qps);
            performanceManager.run();
        } catch (Exception e) {
            System.out.println("Error: " + e + " please check logs/error.log");
        }
    }

    public static void sendTransactionTest(
            String chainName, String accountName, BigInteger count, BigInteger qps) {
        try {
            PerformanceSuite suite = new PureBCOSSendTransactionSuite(chainName, accountName);
            PerformanceManager performanceManager = new PerformanceManager(suite, count, qps);
            performanceManager.run();
        } catch (Exception e) {
            System.out.println("Error: " + e + " please check logs/error.log");
        }
    }

    private static void exit() {
        System.exit(0);
    }
}
