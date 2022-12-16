package com.webank.wecross.stub.bcos3;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import com.webank.wecross.stub.bcos3.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import com.webank.wecross.stub.bcos3.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.bcos3.custom.DeployContractHandler;
import com.webank.wecross.stub.bcos3.custom.DeployContractWasmHandler;
import com.webank.wecross.stub.bcos3.custom.LinkBfsHandler;
import com.webank.wecross.stub.bcos3.preparation.HubContractDeployment;
import com.webank.wecross.stub.bcos3.preparation.ProxyContractDeployment;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSBaseStubFactory implements StubFactory {
    private final Logger logger = LoggerFactory.getLogger(BCOSBaseStubFactory.class);

    private final String alg;
    private final String stubType;

    private final CryptoSuite cryptoSuite;
    private final BCOSAccountFactory bcosAccountFactory;

    public BCOSBaseStubFactory(int encryptType, String alg, String stubType) {
        this.alg = alg;
        this.stubType = stubType;
        this.cryptoSuite = new CryptoSuite(encryptType);
        this.bcosAccountFactory = BCOSAccountFactory.getInstance(this.cryptoSuite);

        logger.info(" EncryptType: {}", this.cryptoSuite.getCryptoTypeConfig());
    }

    @Override
    public void init(WeCrossContext context) {}

    public boolean isGMStub() {
        return StringUtils.containsIgnoreCase(stubType, BCOSConstant.GM);
    }

    public boolean isWASMStub() {
        return StringUtils.containsIgnoreCase(stubType, BCOSConstant.WASM);
    }

    public String getAlg() {
        return alg;
    }

    public String getStubType() {
        return stubType;
    }

    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    @Override
    public Driver newDriver() {
        logger.info("New driver type:{}", this.cryptoSuite.getCryptoTypeConfig());

        /** Initializes the bfs service */
        AsyncBfsService asyncBfsService = new AsyncBfsService();

        /** Initializes the custom command dispatcher */
        LinkBfsHandler linkBfsHandler = new LinkBfsHandler();
        linkBfsHandler.setAsyncBfsService(asyncBfsService);

        DeployContractHandler deployContractHandler = new DeployContractHandler();
        deployContractHandler.setAsyncBfsService(asyncBfsService);

        DeployContractWasmHandler deployContractWasmHandler = new DeployContractWasmHandler();
        deployContractWasmHandler.setAsyncBfsService(asyncBfsService);

        CommandHandlerDispatcher commandHandlerDispatcher = new CommandHandlerDispatcher();
        commandHandlerDispatcher.registerCommandHandler(
                BCOSConstant.CUSTOM_COMMAND_REGISTER, linkBfsHandler);
        commandHandlerDispatcher.registerCommandHandler(
                BCOSConstant.CUSTOM_COMMAND_DEPLOY, deployContractHandler);
        commandHandlerDispatcher.registerCommandHandler(
                BCOSConstant.CUSTOM_COMMAND_DEPLOY_WASM, deployContractWasmHandler);

        /** Initializes the bcos driver */
        BCOSDriver driver = new BCOSDriver(this.cryptoSuite, isWASMStub());
        driver.setAsyncBfsService(asyncBfsService);
        driver.setCommandHandlerDispatcher(commandHandlerDispatcher);

        asyncBfsService.setBcosDriver(driver);

        return driver;
    }

    @Override
    public Connection newConnection(String path) {
        try {
            logger.info("New connection: {} type:{}", path, this.cryptoSuite.getCryptoTypeConfig());
            BCOSConnection connection = BCOSConnectionFactory.build(path, "stub.toml");

            // check proxy contract
            if (!connection.hasProxyDeployed()) {
                String errorMsg =
                        "WeCrossProxy error: WeCrossProxy contract has not been deployed!";
                String help =
                        "Please deploy WeCrossProxy contract by: "
                                + ProxyContractDeployment.getUsage(path);
                System.out.println(errorMsg + "\n" + help);
                throw new Exception(errorMsg);
            }

            // check hub contract
            if (!connection.hasHubDeployed()) {
                String errorMsg = "WeCrossHub error: WeCrossHub contract has not been deployed!";
                String help =
                        "Please deploy WeCrossHub contract by: "
                                + HubContractDeployment.getUsage(path);
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
    public Account newAccount(Map<String, Object> properties) {

        return bcosAccountFactory.build(properties);
    }

    public Account newAccount(String name, String path) {
        try {
            logger.info("New account: {} type:{}", name, this.cryptoSuite.getCryptoTypeConfig());
            return bcosAccountFactory.build(
                    name, path.startsWith("classpath") ? path : "file:" + path);
        } catch (Exception e) {
            logger.warn(" newAccount, e: ", e);
            return null;
        }
    }

    @Override
    public void generateAccount(String path, String[] args) {
        try {
            /** create KeyPair first */
            Security.addProvider(new BouncyCastleProvider());

            CryptoKeyPair keyPair = cryptoSuite.getKeyPairFactory().generateKeyPair();
            PrivateKey ecPrivateKey = keyPair.keyPair.getPrivate();
            String accountAddress = keyPair.getAddress();

            /** write private to file in pem format */
            String keyFile = path + "/" + accountAddress + "_" + getAlg() + ".key";
            File file = new File(keyFile);

            if (!file.getParentFile().exists()) {

                if (!file.getParentFile().mkdirs()) {
                    System.out.println("Account dir:" + file.getParent() + " create failed");
                }
            }

            if (!file.createNewFile()) {
                System.out.println("Key file exists!" + keyFile);
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
                            + "    type='"
                            + getStubType()
                            + "'\n"
                            + "    accountFile='"
                            + file.getName()
                            + "'\n"
                            + "    password='' # if use *.p12 accountFile";
            String confFilePath = path + "/account.toml";
            File confFile = new File(confFilePath);
            if (!confFile.createNewFile()) {
                System.out.println("Conf file exists! " + confFile);
                return;
            }

            FileWriter fileWriter = new FileWriter(confFile);
            try {
                fileWriter.write(accountTemplate);
            } finally {
                fileWriter.close();
            }

            String name = new File(path).getName();
            System.out.println(
                    "SUCCESS: Account \""
                            + name
                            + "\" config framework has been generated to \""
                            + path
                            + "\"");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    @Override
    public void generateConnection(String path, String[] args) {
        try {
            String chainName = new File(path).getName();

            String accountTemplate =
                    "[common]\n"
                            + "    name = '"
                            + chainName
                            + "'\n"
                            + "    type = '"
                            + getStubType()
                            + "' # BCOS3_ECDSA_EVM、BCOS3_ECDSA_WASM、BCOS3_GM_EVM、BCOS3_GM_WASM\n"
                            + "\n"
                            + "[chain]\n"
                            + "    groupId = 'group0' # default group0\n"
                            + "    chainId = 'chain0' # default chain0\n"
                            + "\n"
                            + "[channelService]\n"
                            + "    caCert = 'ca.crt'\n"
                            + "    sslCert = 'sdk.crt'\n"
                            + "    sslKey = 'sdk.key'\n"
                            + "    gmCaCert = 'sm_ca.crt'\n"
                            + "    gmSslCert = 'sm_sdk.crt'\n"
                            + "    gmSslKey = 'sm_sdk.key'\n"
                            + "    gmEnSslCert = 'sm_ensdk.crt'\n"
                            + "    gmEnSslKey = 'sm_ensdk.key'\n"
                            + "    messageTimeout = 300000  # ms, default 60000ms\n"
                            + "    connectionsStr = ['127.0.0.1:20200']\n"
                            + "\n";
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
            generateHubContract(path);

            generateAccount(path + File.separator + BCOSConstant.ADMIN_ACCOUNT, null);

            System.out.println(
                    "SUCCESS: Chain \""
                            + chainName
                            + "\" config framework has been generated to \""
                            + path
                            + "\"");
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

    public void generateHubContract(String path) {
        try {
            String hubPath = "WeCrossHub.sol";
            URL hubDir = getClass().getResource(File.separator + hubPath);
            File dest = new File(path + File.separator + "WeCrossHub" + File.separator + hubPath);
            FileUtils.copyURLToFile(hubDir, dest);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
