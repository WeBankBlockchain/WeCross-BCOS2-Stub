package com.webank.wecross.stub.bcos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.WeCrossContext;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import com.webank.wecross.stub.bcos.blockheader.BlockManagerEmpty;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.bcos.custom.CustomCommandRequest;
import com.webank.wecross.stub.bcos.custom.DeployContractHandler;
import com.webank.wecross.stub.bcos.custom.RegisterCnsHandler;
import com.webank.wecross.stub.bcos.preparation.HubContractDeployment;
import com.webank.wecross.stub.bcos.preparation.ProxyContractDeployment;
import com.webank.wecross.stub.bcos.web3j.AbstractWeb3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperFactory;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class BCOSBaseStubFactory implements StubFactory {
    private Logger logger = LoggerFactory.getLogger(BCOSBaseStubFactory.class);

    private String alg = null;
    private String stubType = null;

    private ScheduledExecutorService connectionScheduledExecutorService =
            new ScheduledThreadPoolExecutor(16, new CustomizableThreadFactory("BCOSConnection-"));

    public BCOSBaseStubFactory(int encryptType, String alg, String stubType) {
        EncryptType.setEncryptType(encryptType);
        this.alg = alg;
        this.stubType = stubType;
        logger.info(" EncryptType: {}", EncryptType.getEncryptType());
    }

    @Override
    public void init(WeCrossContext context) {}

    /**
     * The algorithm name, secp256k1 or sm2p256v1
     *
     * @return
     */
    public String getAlg() {
        return alg;
    }

    /**
     * The stub type, BCOS2.0 or GM_BCOS2.0
     *
     * @return
     */
    public String getStubType() {
        return stubType;
    }

    @Override
    public Driver newDriver() {
        logger.info("New driver type:{}", EncryptType.encryptType);

        /** Initializes the cns service */
        AsyncCnsService asyncCnsService = new AsyncCnsService();

        /** Initializes the custom command dispatcher */
        RegisterCnsHandler registerCnsHandler = new RegisterCnsHandler();
        registerCnsHandler.setAsyncCnsService(asyncCnsService);

        DeployContractHandler deployContractHandler = new DeployContractHandler();
        deployContractHandler.setAsyncCnsService(asyncCnsService);

        CommandHandlerDispatcher commandHandlerDispatcher = new CommandHandlerDispatcher();
        commandHandlerDispatcher.registerCommandHandler(
                BCOSConstant.CUSTOM_COMMAND_REGISTER, registerCnsHandler);
        commandHandlerDispatcher.registerCommandHandler(
                BCOSConstant.CUSTOM_COMMAND_DEPLOY, deployContractHandler);

        /** Initializes the bcos driver */
        BCOSDriver driver = new BCOSDriver();
        driver.setAsyncCnsService(asyncCnsService);
        driver.setCommandHandlerDispatcher(commandHandlerDispatcher);

        asyncCnsService.setBcosDriver(driver);

        return driver;
    }

    @Override
    public Connection newConnection(String path) {
        try {
            logger.info("New connection: {} type:{}", path, EncryptType.encryptType);
            BCOSConnection connection = BCOSConnectionFactory.build(path, "stub.toml");
            checkConnection(connection, path);
            return connection;
        } catch (Exception e) {
            logger.error(" newConnection, e: ", e);
            return null;
        }
    }

    public Connection newConnection(Map<String, Object> config) {
        try {
            String helpPath = "<your-chain-dir>";
            logger.info("New connection: {} type:{}", helpPath, EncryptType.encryptType);
            BCOSConnection connection = BCOSConnectionFactory.build(config);
            // For luyu protocol, no need to check proxy and hub
            return connection;
        } catch (Exception e) {
            System.out.println(e);
            logger.error(" newConnection, e: ", e);
            return null;
        }
    }

    private void checkConnection(BCOSConnection connection, String path) throws Exception {
        // check proxy contract
        if (!connection.hasProxyDeployed()) {
            String errorMsg = "WeCrossProxy error: WeCrossProxy contract has not been deployed!";
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
                    "Please deploy WeCrossHub contract by: " + HubContractDeployment.getUsage(path);
            System.out.println(errorMsg + "\n" + help);
            throw new Exception(errorMsg);
        }
    }

    @Override
    public Account newAccount(Map<String, Object> properties) {
        BCOSAccountFactory factory = new BCOSAccountFactory();
        return factory.build(properties);
    }

    public Account newAccount(String name, String path) {
        try {
            logger.info("New account: {} type:{}", name, EncryptType.encryptType);
            return BCOSAccountFactory.build(
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

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(getAlg());
            SecureRandom secureRandom = new SecureRandom();
            keyPairGenerator.initialize(ecGenParameterSpec, secureRandom);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            ECKeyPair ecKeyPair = ECKeyPair.create(keyPair);
            Credentials credentials = GenCredential.create(ecKeyPair);
            PrivateKey ecPrivateKey = keyPair.getPrivate();

            String accountAddress = credentials.getAddress();

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
                            + "' # BCOS2.0 or GM_BCOS2.0\n"
                            + "\n"
                            + "[chain]\n"
                            + "    groupId = 1 # default 1\n"
                            + "    chainId = 1 # default 1\n"
                            + "\n"
                            + "[channelService]\n"
                            + "    caCert = 'ca.crt'\n"
                            + "    sslCert = 'sdk.crt'\n"
                            + "    sslKey = 'sdk.key'\n"
                            + ((BCOSConstant.BCOS_STUB_TYPE.equals(getStubType()))
                                    ? "    gmConnectEnable = false\n"
                                    : "    gmConnectEnable = true\n")
                            + "    gmCaCert = 'gm/gmca.crt'\n"
                            + "    gmSslCert = 'gm/gmsdk.crt'\n"
                            + "    gmSslKey = 'gm/gmsdk.key'\n"
                            + "    gmEnSslCert = 'gm/gmensdk.crt'\n"
                            + "    gmEnSslKey = 'gm/gmensdk.key'\n"
                            + "    timeout = 300000  # ms, default 60000ms\n"
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

    public void executeCustomCommand(String accountName, String content) throws Exception {
        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

        CustomCommandRequest request =
                objectMapper.readValue(content, new TypeReference<CustomCommandRequest>() {});

        Path path = Path.decode(request.getPath());

        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser(
                        "chains" + File.separator + path.getChain(), "connection.toml");
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        AbstractWeb3jWrapper web3jWrapper =
                Web3jWrapperFactory.createWeb3jWrapperInstance(bcosStubConfig);

        BCOSAccount account =
                (BCOSAccount)
                        newAccount(
                                accountName, "classpath:accounts" + File.separator + accountName);

        Connection connection =
                BCOSConnectionFactory.build(
                        bcosStubConfig, web3jWrapper, connectionScheduledExecutorService);

        if (account == null) {
            throw new Exception("Account " + accountName + " not found");
        }

        if (connection == null) {
            throw new Exception("Init connection exception, please check log");
        }

        Driver driver = newDriver();
        CompletableFuture<Exception> future = new CompletableFuture<>();
        driver.asyncCustomCommand(
                request.getCommand(),
                path,
                request.getArgs().toArray(new Object[0]),
                account,
                new BlockManagerEmpty(),
                connection,
                new Driver.CustomCommandCallback() {
                    @Override
                    public void onResponse(Exception error, Object response) {
                        if (error != null) {
                            System.out.println(error.getMessage());
                        } else {
                            System.out.println(
                                    "Success on command: "
                                            + request.getCommand()
                                            + " path: "
                                            + request.getPath());
                        }
                        future.complete(error);
                    }
                });
        try {
            Exception e = future.get(30, TimeUnit.SECONDS);
            if (e != null) {
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
