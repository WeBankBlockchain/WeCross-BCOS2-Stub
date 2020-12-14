package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.fisco.EnumNodeVersion;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class Web3jUtility {

    private static final Logger logger = LoggerFactory.getLogger(Web3jUtility.class);

    private Web3jUtility() {}

    public static ThreadPoolTaskExecutor build(int threadNum, int queueCapacity, String name) {
        logger.info(
                " initializing Web3j ThreadPoolTaskExecutor, threadC: {}, threadName: {}",
                threadNum,
                name);
        // init default thread pool

        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(threadNum);
        threadPool.setMaxPoolSize(threadNum);
        threadPool.setQueueCapacity(queueCapacity);
        threadPool.setThreadNamePrefix(name);
        threadPool.initialize();
        return threadPool;
    }

    /**
     * Initialize the web3j service with check the check the configuration
     *
     * @param bcosStubConfig
     * @return
     * @throws Exception
     */
    public static Web3j initWeb3j(BCOSStubConfig bcosStubConfig) throws Exception {

        logger.info("BCOSStubConfig: {}", bcosStubConfig);

        BCOSStubConfig.ChannelService channelService = bcosStubConfig.getChannelService();
        Web3j web3j = initWeb3j(channelService);

        NodeVersion.Version version = web3j.getNodeVersion().send().getNodeVersion();

        logger.info("NodeVersion: {}", version);

        // check version
        checkVersion(version);
        // check config
        checkConfig(version, bcosStubConfig.getType());

        return web3j;
    }

    /**
     * Check the stub config type
     *
     * @param version
     * @param stubType
     */
    public static void checkConfig(NodeVersion.Version version, String stubType) throws Exception {
        boolean isGMStub = stubType.toLowerCase().contains("gm");
        boolean isGMNode = version.getVersion().toLowerCase().contains("gm");
        if (logger.isDebugEnabled()) {
            logger.debug(" isGMStub: {}, isGMNode: {}", isGMStub, isGMNode);
        }
        if (!(isGMStub == isGMNode)) {
            throw new Exception(
                    "Please check config "
                            + "stub.toml common::type field, change to \""
                            + (isGMNode ? "GM_BCOS2.0" : "BCOS2.0")
                            + "\"");
        }
    }

    /**
     * Check the node version information, 2.4.0+ supported
     *
     * @param version
     * @throws Exception
     */
    public static void checkVersion(NodeVersion.Version version) throws Exception {

        String supportedVersionStr = version.getSupportedVersion();
        String nodeVersionStr = version.getVersion();

        EnumNodeVersion.Version supportedVersion =
                EnumNodeVersion.getClassVersion(supportedVersionStr);

        /*2.4.0 gm or 2.4.0*/
        String[] strings = nodeVersionStr.split(" ");
        EnumNodeVersion.Version nodeVersion = EnumNodeVersion.getClassVersion(strings[0]);

        // must not below than 2.4.0
        if (!(supportedVersion.getMajor() == 2 && supportedVersion.getMinor() >= 4)) {
            throw new Exception(
                    "FISCO BCOS supported version is not supported, version must not below than 2.4.0, but current is "
                            + supportedVersionStr);
        }

        // must not below than 2.4.0
        if (!(nodeVersion.getMajor() == 2 && nodeVersion.getMinor() >= 4)) {
            throw new Exception(
                    "FISCO BCOS version is not supported, version must not below than 2.4.0, but current is "
                            + nodeVersionStr);
        }
    }

    /**
     * Initialize the web3j service
     *
     * @param channelServiceConfig
     * @return Web3J object
     * @throws Exception
     */
    public static Web3j initWeb3j(BCOSStubConfig.ChannelService channelServiceConfig)
            throws Exception {

        logger.info(" ChannelService: {}", channelServiceConfig);

        List<ChannelConnections> allChannelConnections = new ArrayList<>();
        ChannelConnections channelConnections = new ChannelConnections();

        channelConnections.setEnableOpenSSL(false);
        channelConnections.setGroupId(channelServiceConfig.getChain().getGroupID());
        channelConnections.setConnectionsStr(channelServiceConfig.getConnectionsStr());
        allChannelConnections.add(channelConnections);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        GroupChannelConnectionsConfig groupChannelConnectionsConfig =
                new GroupChannelConnectionsConfig();

        groupChannelConnectionsConfig.setCaCert(
                resolver.getResource(channelServiceConfig.getCaCert()));
        groupChannelConnectionsConfig.setSslCert(
                resolver.getResource(channelServiceConfig.getSslCert()));
        groupChannelConnectionsConfig.setSslKey(
                resolver.getResource(channelServiceConfig.getSslKey()));
        groupChannelConnectionsConfig.setAllChannelConnections(allChannelConnections);

        Service service = new Service();
        service.setThreadPool(
                build(
                        channelServiceConfig.getThreadNum(),
                        channelServiceConfig.getQueueCapacity(),
                        "web3j_callback"));

        service.setGroupId(channelServiceConfig.getChain().getGroupID());
        service.setAllChannelConnections(groupChannelConnectionsConfig);

        /** service run */
        service.run();

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        channelEthereumService.setTimeout(channelServiceConfig.getTimeout());
        return org.fisco.bcos.web3j.protocol.Web3j.build(
                channelEthereumService, service.getGroupId());
    }
}
