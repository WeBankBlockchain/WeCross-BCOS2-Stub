package com.webank.wecross.stub.bcos3.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos3.performance.PerformanceManager;
import java.math.BigInteger;
import java.util.Objects;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceTest {

    private static Logger logger = LoggerFactory.getLogger(PerformanceTest.class);

    public static void usage() {
        System.err.println("Usage:");

        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " [chainName] [accountName] [contractName] call [count] [qps] [enableGM] [isWasm]");
        System.err.println(
                " \t java -cp conf/:lib/*:plugin/* "
                        + PerformanceTest.class.getName()
                        + " [chainName] [accountName] [contractName] sendTransaction [count] [qps] [enableGM] [isWasm]");
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
        if (args.length < 7) {
            usage();
        }

        String chainName = args[0];
        String accountName = args[1];
        String contractName = args[2];
        String command = args[3];
        BigInteger count = new BigInteger(args[4]);
        BigInteger qps = new BigInteger(args[5]);
        boolean sm = false;
        boolean isWasm = Boolean.parseBoolean(args[7]);
        if (args.length > 7) {
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
                        + sm
                        + ", isWasm: "
                        + isWasm);

        try {
            PureBCOSProxySuite suite =
                    "sendTransaction".equals(command)
                            ? new PureBCOSProxySendTransactionSuite(
                                    contractName, chainName, accountName, sm, isWasm)
                            : new PureBCOSProxyCallSuite(contractName, chainName, accountName, sm);
            BFSInfo bfsInfo = suite.getBfsInfo();
            if (Objects.isNull(bfsInfo)) {
                System.err.println(" ## Error: unable to fetch proxy contract address. ");
                System.exit(0);
            }
            System.err.println(" ## Proxy contract address: " + bfsInfo.getAddress());

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
