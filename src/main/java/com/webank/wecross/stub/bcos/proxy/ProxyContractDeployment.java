package com.webank.wecross.stub.bcos.proxy;

import java.io.File;

public class ProxyContractDeployment {
    public static String USAGE =
            "Usage:\n"
                    + "         java -cp conf/:lib/*:plugin/* "
                    + ProxyContractDeployment.class.getName()
                    + " check [chainName]\n"
                    + "         java -cp conf/:lib/*:plugin/* "
                    + ProxyContractDeployment.class.getName()
                    + " deploy [chainName] [accountName]\n"
                    + "Example:\n"
                    + "         java -cp conf/:lib/*:plugin/* "
                    + ProxyContractDeployment.class.getName()
                    + " check chains/bcos\n"
                    + "         java -cp conf/:lib/*:plugin/* "
                    + ProxyContractDeployment.class.getName()
                    + " deploy chains/bcos bcos_user1";

    public static void usage() {
        System.out.println(USAGE);
        exit();
    }

    private static void exit() {
        System.exit(0);
    }

    private static void exit(int sig) {
        System.exit(sig);
    }

    public static void main(String[] args) throws Exception {
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
            System.out.println(e);
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
            default:
                usage();
        }
    }

    public static void check(String chainPath) {
        ProxyContract.check(chainPath);
    }

    public static void deploy(String chainPath, String accountName) {
        try {
            String proxyContractDir = chainPath + File.separator + "WeCrossProxy" + File.separator;
            ProxyContract proxyContract =
                    new ProxyContract(proxyContractDir, chainPath, accountName);
            proxyContract.deploy();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
