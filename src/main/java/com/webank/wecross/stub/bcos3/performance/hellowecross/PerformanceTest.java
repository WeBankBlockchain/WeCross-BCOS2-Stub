package com.webank.wecross.stub.bcos3.performance.hellowecross;

import com.webank.wecross.stub.bcos3.performance.PerformanceManager;
import com.webank.wecross.stub.bcos3.performance.PerformanceSuite;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceTest {

    private static Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    public static void usage() {
        System.err.println("Usage:");

        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " [chainName] [accountName] call [count] [qps] [enableGM]");
        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " [chainName] [accountName] sendTransaction [count] [qps] [enableGM]");
        System.err.println("Example:");
        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " chains/bcos bcos_user1 call 10000 1000");
        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " chains/bcos bcos_user1 sendTransaction 10000 1000");

        exit();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            usage();
        }

        String chainName = args[0];
        String accountName = args[1];
        String command = args[2];
        BigInteger count = new BigInteger(args[3]);
        BigInteger qps = new BigInteger(args[4]);
        boolean sm = false;
        if (args.length > 5) {
            sm = Boolean.valueOf(args[5]);
        }

        System.err.println(
                "BCOSPerformanceTest [HelloWeCross]: "
                        + ", command: "
                        + command
                        + ", chain: "
                        + chainName
                        + ", account: "
                        + accountName
                        + ", count: "
                        + count
                        + ", qps: "
                        + qps
                        + ", enableGM: "
                        + sm);

        try {
            PerformanceSuite suite =
                    command.equals("sendTransaction")
                            ? new PureBCOSSendTransactionSuite(chainName, accountName, sm)
                            : new PureBCOSCallSuite(chainName, accountName, sm);
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
