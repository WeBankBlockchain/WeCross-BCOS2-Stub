package com.webank.wecross.stub.bcos.preparation;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubContractDeployment {

    private static final Logger logger = LoggerFactory.getLogger(HubContractDeployment.class);

    public static void usage() {
        System.out.println(getUsage("chains/bcos"));
        exit();
    }

    public static String getUsage(String chainPath) {
        String pureChainPath = chainPath.replace("classpath:/", "").replace("classpath:", "");
        return "Usage:\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubContractDeployment.class.getName()
                + " check [chainName]\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubContractDeployment.class.getName()
                + " deploy [chainName] [accountName]\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubContractDeployment.class.getName()
                + " upgrade [chainName] [accountName]\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubContractDeployment.class.getName()
                + " getAddress [chainName]\n"
                + "Example:\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubContractDeployment.class.getName()
                + " check "
                + pureChainPath
                + "\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubContractDeployment.class.getName()
                + " deploy "
                + pureChainPath
                + " bcos_user1\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubContractDeployment.class.getName()
                + " upgrade "
                + pureChainPath
                + " bcos_user1\n"
                + "         java -cp 'conf/:lib/*:plugin/*' "
                + HubContractDeployment.class.getName()
                + " getAddress "
                + pureChainPath
                + "\n";
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
            System.out.println("Failed, please check account or contract.");
            logger.warn("Error: ", e);
        } finally {
            exit();
        }
    }

    public static void handle2Args(String[] args) throws Exception {
        if (args.length != 2) {
            usage();
        }

        String cmd = args[0];
        String chainPath = args[1];

        switch (cmd) {
            case "check":
                check(chainPath);
                break;
            case "getAddress":
                getAddress(chainPath);
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

    public static void check(String chainPath) {
        HubContract.check(chainPath);
    }

    public static void getAddress(String chainPath) {
        HubContract.getHubAddress(chainPath);
    }

    public static void deploy(String chainPath, String accountName) {
        try {
            String hubContractFile =
                    chainPath + File.separator + "WeCrossHub" + File.separator + "WeCrossHub.sol";
            HubContract hubContract = new HubContract(hubContractFile, chainPath, accountName);
            hubContract.deploy();
        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println(e);
        }
    }

    public static void upgrade(String chainPath, String accountName) {
        try {
            String hubContractFile =
                    chainPath + File.separator + "WeCrossHub" + File.separator + "WeCrossHub.sol";
            HubContract hubContract = new HubContract(hubContractFile, chainPath, accountName);
            hubContract.upgrade();
        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println(e);
        }
    }
}
