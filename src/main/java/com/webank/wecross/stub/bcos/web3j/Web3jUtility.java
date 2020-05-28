package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class Web3jUtility {

    private static final Logger logger = LoggerFactory.getLogger(Web3jUtility.class);

    private Web3jUtility() {}

    public static ThreadPoolTaskExecutor buildThreadPool(
            int threadNum, int queueCapacity, String name) {
        logger.info(
                " initializing Web3j ThreadPoolTaskExecutor, threadC: {}, queueCapacity: {}, threadName: {}",
                threadNum,
                queueCapacity,
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
                buildThreadPool(
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
