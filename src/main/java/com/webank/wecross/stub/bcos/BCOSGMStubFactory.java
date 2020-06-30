package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.bcos.proxy.ProxyContractDeployment;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stub("GM_BCOS2.0")
public class BCOSGMStubFactory implements StubFactory {
    private Logger logger = LoggerFactory.getLogger(BCOSGMStubFactory.class);

    public BCOSGMStubFactory() {
        EncryptType encryptType = new EncryptType(EncryptType.SM2_TYPE);
        logger.info(" EncryptType: {}", encryptType.getEncryptType());
    }

    @Override
    public void init(WeCrossContext context) {}

    @Override
    public Driver newDriver() {
        logger.info("New driver type:{}", EncryptType.encryptType);
        BCOSDriver driver = new BCOSDriver();
        CommandHandlerDispatcher commandHandlerDispatcher = new CommandHandlerDispatcher();
        commandHandlerDispatcher.initializeCommandMapper();
        driver.setCommandHandlerDispatcher(commandHandlerDispatcher);
        return driver;
    }

    @Override
    public Connection newConnection(String path) {
        try {
            logger.info("New connection: {} type:{}", path, EncryptType.encryptType);
            BCOSConnection connection = BCOSConnectionFactory.build(path, "stub.toml", null);

            // check proxy contract
            if (!connection.hasProxyDeployed()) {
                String errorMsg =
                        "WeCrossProxy error: WeCrossProxy contract has not been deployed!";
                String help =
                        "Please deploy WeCrossProxy contract by: " + ProxyContractDeployment.USAGE;
                System.out.println(errorMsg + "\n" + help);
                throw new Exception(errorMsg);
            }
            return connection;
        } catch (Exception e) {
            logger.error(" newConnection, e: ", e);
            return null;
        }
    }

    @Override
    public Account newAccount(String name, String path) {
        try {
            logger.info("New account: {} type:{}", name, EncryptType.encryptType);
            return BCOSAccountFactory.build(
                    name, path.startsWith("classpath") ? path : "file:" + path);
        } catch (Exception e) {
            logger.error(" newAccount, e: ", e);
            return null;
        }
    }

    @Override
    public void generateAccount(String path, String[] args) {
        try {
            // Write pem file
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator keyPairGenerator =
                    KeyPairGenerator.getInstance("EC", Security.getProvider("BC"));

            keyPairGenerator.initialize(256);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            PrivateKey ecPrivateKey = keyPair.getPrivate();

            String keyFile = path + "/account.key";
            File file = new File(keyFile);

            if (!file.createNewFile()) {
                logger.error("Key file exists! {}", keyFile);
                return;
            }

            PemWriter pemWriter = new PemWriter(new FileWriter(file));
            try {
                pemWriter.writeObject(new PemObject("PRIVATE KEY", ecPrivateKey.getEncoded()));
            } finally {
                pemWriter.close();
            }

            String accountTemplate =
                    "[account]\n"
                            + "    type='GM_BCOS2.0'\n"
                            + "    accountFile='account.key'\n"
                            + "    password=''";
            String confFilePath = path + "/account.toml";
            File confFile = new File(confFilePath);
            if (!confFile.createNewFile()) {
                logger.error("Conf file exists! {}", confFile);
                return;
            }

            FileWriter fileWriter = new FileWriter(confFile);
            try {
                fileWriter.write(accountTemplate);
            } finally {
                fileWriter.close();
            }

        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }

    @Override
    public void generateConnection(String path, String[] args) {
        try {
            String accountTemplate =
                    "[common]\n"
                            + ""
                            + "    type = 'GM_BCOS2.0' # BCOS\n"
                            + "\n"
                            + "[chain]\n"
                            + "    groupId = 1 # default 1\n"
                            + "    chainId = 1 # default 1\n"
                            + "    enableGM = false # default false\n"
                            + "\n"
                            + "[channelService]\n"
                            + "    caCert = 'ca.crt'\n"
                            + "    sslCert = 'sdk.crt'\n"
                            + "    sslKey = 'sdk.key'\n"
                            + "    timeout = 300000  # ms, default 60000ms\n"
                            + "    connectionsStr = ['127.0.0.1:20200']\n"
                            + "\n"
                            + "# resources is a list\n"
                            + "[[resources]]\n"
                            + "    # name cannot be repeated\n"
                            + "    name = 'HelloWeCross'\n"
                            + "    type = 'BCOS_CONTRACT'\n"
                            + "    contractAddress = '0x0'";
            String confFilePath = path + "/stub.toml";
            File confFile = new File(confFilePath);
            if (!confFile.createNewFile()) {
                logger.error("Conf file exists! {}", confFile);
                return;
            }

            FileWriter fileWriter = new FileWriter(confFile);
            try {
                fileWriter.write(accountTemplate);
            } finally {
                fileWriter.close();
            }

            generateProxyContract(path);

        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }

    public void generateProxyContract(String path) {
        try {
            String proxyPath = "WeCrossProxy.sol";
            URL proxyDir = getClass().getResource(File.separator + proxyPath);
            File dest =
                    new File(path + File.separator + "WeCrossProxy" + File.separator + proxyPath);
            FileUtils.copyURLToFile(proxyDir, dest);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(
                "This is BCOS2.0 Guomi Stub Plugin. Please copy this file to router/plugin/");
        System.out.println("For deploy proxy contract:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.proxy.ProxyContractDeployment");
        System.out.println(
                "For pure chain performance test, please run the command for more info:");
        System.out.println(
                "    java -cp conf/:lib/*:plugin/* com.webank.wecross.stub.bcos.guomi.performance.guomi.PerformanceTest");
    }
}
