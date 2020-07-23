package com.webank.wecross.stub.bcos.performance.parallel.proxy;

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
                " \t java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.performance.parallel.proxy.PerformanceTest [chain] [path/address] [account] [proxyAddress] [totalTXCount] [QPS] [userFile]");
        System.out.println("Example:");
        System.out.println(
                " \t java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.performance.parallel.proxy.PerformanceTest chains/bcos payment.bcos1.transfer bcos_user1/0x5002 0x11111 10000 1000 ./user");

        exit();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 7) {
            usage();
        }

        String chain = args[0];
        String pathOrAddress = args[1];
        String account = args[2];
        String proxyAddress = args[3];
        BigInteger totalCount = new BigInteger(args[4]);
        BigInteger QPS = new BigInteger(args[5]);
        String userFile = args[6];

        System.out.println(
                "Parallel Proxy: "
                        + ", chain: "
                        + chain
                        + ", pathOrAddress: "
                        + pathOrAddress
                        + ", account: "
                        + account
                        + ", proxyAddress: "
                        + proxyAddress
                        + ", totalCount: "
                        + totalCount
                        + ", QPS: "
                        + QPS
                        + ", userFile: "
                        + userFile);

        sendTransactionTest(chain, pathOrAddress, account, proxyAddress, userFile, totalCount, QPS);
    }

    public static void sendTransactionTest(
            String chain,
            String pathOrAddress,
            String account,
            String proxyAddress,
            String userFile,
            BigInteger totalCount,
            BigInteger QPS) {
        try {
            PerformanceSuite suite =
                    new BCOSProxyParallelSuite(
                            chain, account, pathOrAddress, proxyAddress, userFile);
            PerformanceManager performanceManager = new PerformanceManager(suite, totalCount, QPS);
            performanceManager.run();
        } catch (Exception e) {
            System.out.println("Error: " + e + " please check logs/error.log");
        }
    }

    private static void exit() {
        System.exit(0);
    }
}
