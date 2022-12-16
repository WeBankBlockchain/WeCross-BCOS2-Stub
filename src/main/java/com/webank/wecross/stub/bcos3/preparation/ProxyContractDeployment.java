package com.webank.wecross.stub.bcos3.preparation;

import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyContractDeployment {

    private static final Logger logger = LoggerFactory.getLogger(ProxyContractDeployment.class);

    public static void usage() {
        System.out.println(getUsage("chains/bcos"));
        exit();
    }

    public static String getUsage(String chainPath) {
        String pureChainPath = chainPath.replace("classpath:/", "").replace("classpath:", "");
        return "Usage:\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + ProxyContractDeployment.class.getName()
                + " deploy [chainName] [accountName(optional)]\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + ProxyContractDeployment.class.getName()
                + " upgrade [chainName] [accountName(optional)]\n"
                + "Example:\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + ProxyContractDeployment.class.getName()
                + " deploy "
                + pureChainPath
                + " \n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + ProxyContractDeployment.class.getName()
                + " deploy "
                + pureChainPath
                + " admin\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + ProxyContractDeployment.class.getName()
                + " upgrade "
                + pureChainPath
                + " \n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + ProxyContractDeployment.class.getName()
                + " upgrade "
                + pureChainPath
                + " admin";
    }

    private static void exit() {
        System.exit(0);
    }

    private static void exit(int sig) {
        System.exit(sig);
    }

    public static void main(String[] args) {
        try {
            switch (args.length) {
                case 2:
                    handle2Args(args);
                    break;
                case 3:
                    handle3Args(args);
                    break;
                default:
                    usage();
            }
        } catch (Exception e) {
            System.out.println("Failed, please check account or contract. " + e);
            logger.warn("Error: ", e);
        } finally {
            exit();
        }
    }

    public static void handle2Args(String[] args) {
        if (args.length != 2) {
            usage();
        }

        String cmd = args[0];
        String chainPath = args[1];
        String accountName = BCOSConstant.ADMIN_ACCOUNT;

        switch (cmd) {
            case "deploy":
                deploy(chainPath, accountName);
                break;
            case "upgrade":
                upgrade(chainPath, accountName);
                break;
            default:
                usage();
        }
    }

    public static void handle3Args(String[] args) throws Exception {
        if (args.length != 3) {
            usage();
        }

        String cmd = args[0];
        String chainPath = args[1];
        String accountName = args[2];

        switch (cmd) {
            case "deploy":
                deploy(chainPath, accountName);
                break;
            case "upgrade":
                upgrade(chainPath, accountName);
                break;
            default:
                usage();
        }
    }

    public static void deploy(String chainPath, String accountName) {
        try {
            String proxyContractFile =
                    chainPath
                            + File.separator
                            + "WeCrossProxy"
                            + File.separator
                            + "WeCrossProxy.sol";
            ProxyContract proxyContract =
                    new ProxyContract(proxyContractFile, chainPath, accountName);
            proxyContract.deploy();
        } catch (Exception e) {
            logger.error("deploy, e: ", e);
            System.out.println("Failed, please check contract or account. Exception details:");
            e.printStackTrace();
        }
    }

    public static void upgrade(String chainPath, String accountName) {
        try {
            String proxyContractFile =
                    chainPath
                            + File.separator
                            + "WeCrossProxy"
                            + File.separator
                            + "WeCrossProxy.sol";
            ProxyContract proxyContract =
                    new ProxyContract(proxyContractFile, chainPath, accountName);
            proxyContract.upgrade();
        } catch (Exception e) {
            logger.error("upgrade, e: ", e);
            System.out.println("Failed, please check contract or account. Exception details:");
            e.printStackTrace();
        }
    }
}
