package com.webank.wecross.stub.bcos.preparation;

import com.webank.wecross.stub.bcos.AsyncBfsService;
import com.webank.wecross.stub.bcos.client.AbstractClientWrapper;
import com.webank.wecross.stub.bcos.custom.DeployContractHandler;
import java.io.File;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class TwoPCContractDeployment {
    private static final Logger logger = LoggerFactory.getLogger(TwoPCContractDeployment.class);

    public static void usage() {
        System.out.println(getUsage("chains/bcos1"));
        exit();
    }

    public static String getUsage(String chainPath) {
        return "Usage:\n"
                + "         java -cp conf/:lib/*:plugin/* "
                + TwoPCContractDeployment.class.getName()
                + " deploy [chainName] [accountName] [contractName] [version] [contractPath] [TPS] [fromIndex] [totalCount]\n"
                + "         java -cp conf/:lib/*:plugin/* "
                + TwoPCContractDeployment.class.getName()
                + " deploy [chainName] [accountName] [contractName] [version] [contractPath] [TPS] [totalCount]\n"
                + "Example:\n"
                + "         java -cp conf/:lib/*:plugin/* "
                + TwoPCContractDeployment.class.getName()
                + " deploy "
                + chainPath
                + " admin HelloWorld v1.1 conf/solidity/HelloWorld.sol 100 10 100\n";
    }

    private static void exit() {
        System.exit(0);
    }

    private static void exit(int sig) {
        System.exit(sig);
    }

    public static void main(String[] args) {
        if (args.length < 7) {
            usage();
        }

        String chainName = args[1];
        String accountName = args[2];
        String contractName = args[3];
        String version = args[4];
        String contractPath = args[5];
        int tps = Integer.parseInt(args[6]);

        int fromIndex = 0;
        int toIndex = 0;
        if (args.length > 8) {
            fromIndex = Integer.parseInt(args[7]);
            toIndex = fromIndex + Integer.parseInt(args[8]);
        } else {
            fromIndex = 0;
            toIndex = Integer.parseInt(args[7]);
        }

        if ((fromIndex < 0) || (toIndex < 0) || (toIndex < fromIndex)) {
            System.out.println(
                    " Invalid from/to parameter, from: " + fromIndex + " ,to: " + toIndex);
            System.exit(0);
        }

        System.out.println(
                " ## 2PC contract deployment"
                        + " ,chainName: "
                        + chainName
                        + " ,accountName: "
                        + accountName
                        + " ,contractName: "
                        + contractName
                        + " ,contractPath: "
                        + contractPath
                        + " ,TPS: "
                        + tps
                        + " ,version: "
                        + version
                        + " ,fromIndex: "
                        + fromIndex
                        + "  ,toIndex: "
                        + toIndex);

        deploy(
                chainName,
                accountName,
                contractName,
                version,
                contractPath,
                tps,
                fromIndex,
                toIndex);

        System.out.println(" ## 2PC contract deployment complete. ");
        System.exit(0);
    }

    public static void deploy(
            String chainName,
            String accountName,
            String contractName,
            String version,
            String contractPath,
            int tps,
            int fromIndex,
            int toIndex) {
        try {
            AsyncBfsService asyncBfsService = new AsyncBfsService();
            DeployContractHandler deployContractHandler = new DeployContractHandler();
            deployContractHandler.setAsyncCnsService(asyncBfsService);

            ProxyContract proxyContract = new ProxyContract(null, chainName, accountName);
            TwoPCContract twoPCContract =
                    new TwoPCContract(proxyContract.getAccount(), proxyContract.getConnection());
            twoPCContract.setDeployContractHandler(deployContractHandler);

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();

            if (!contractPath.startsWith("classpath")) {
                contractPath = "file:" + contractPath;
            }

            File file = resolver.getResource(contractPath).getFile();
            AbstractClientWrapper clientWrapper = proxyContract.getConnection().getClientWrapper();
            twoPCContract.deploy2PCContract(
                    contractName,
                    version,
                    new String(Files.readAllBytes(file.toPath())),
                    tps,
                    fromIndex,
                    toIndex,
                    clientWrapper.getCryptoSuite());

        } catch (Exception e) {
            logger.error("e: ", e);
            System.out.println(e);
            exit();
        }
    }
}
