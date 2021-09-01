package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class Web3jUtility {

    private static final Logger logger = LoggerFactory.getLogger(Web3jUtility.class);

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

    public static Service initService(BCOSStubConfig.ChannelService channelServiceConfig)
            throws Exception {
        logger.info(" ChannelService: {}", channelServiceConfig);

        List<ChannelConnections> allChannelConnections = new ArrayList<>();
        ChannelConnections channelConnections = new ChannelConnections();

        // channelConnections.setEnableOpenSSL(false);
        channelConnections.setGroupId(channelServiceConfig.getChain().getGroupID());
        channelConnections.setConnectionsStr(channelServiceConfig.getConnectionsStr());
        allChannelConnections.add(channelConnections);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        GroupChannelConnectionsConfig groupChannelConnectionsConfig =
                new GroupChannelConnectionsConfig();

        groupChannelConnectionsConfig.setAllChannelConnections(allChannelConnections);

        if (channelServiceConfig.isGmConnectEnable()) {
            checkCertExist(resolver, channelServiceConfig.getGmCaCert(), "GmCaCert");
            groupChannelConnectionsConfig.setGmCaCert(
                    resolver.getResource(channelServiceConfig.getGmCaCert()));

            checkCertExist(resolver, channelServiceConfig.getGmSslKey(), "GmSslKey");
            groupChannelConnectionsConfig.setGmSslKey(
                    resolver.getResource(channelServiceConfig.getGmSslKey()));

            checkCertExist(resolver, channelServiceConfig.getGmEnSslCert(), "GmEnSslCert");
            groupChannelConnectionsConfig.setGmEnSslCert(
                    resolver.getResource(channelServiceConfig.getGmEnSslCert()));

            checkCertExist(resolver, channelServiceConfig.getGmEnSslKey(), "GmEnSslKey");
            groupChannelConnectionsConfig.setGmEnSslKey(
                    resolver.getResource(channelServiceConfig.getGmEnSslKey()));

            checkCertExist(resolver, channelServiceConfig.getGmEnSslCert(), "GmEnSslCert");
            groupChannelConnectionsConfig.setGmSslCert(
                    resolver.getResource(channelServiceConfig.getGmSslCert()));
            EncryptType.setEncryptType(EncryptType.SM2_TYPE);
        } else {
            checkCertExist(resolver, channelServiceConfig.getCaCert(), "CaCert");
            groupChannelConnectionsConfig.setCaCert(
                    resolver.getResource(channelServiceConfig.getCaCert()));

            checkCertExist(resolver, channelServiceConfig.getSslCert(), "SslCert");
            groupChannelConnectionsConfig.setSslCert(
                    resolver.getResource(channelServiceConfig.getSslCert()));

            checkCertExist(resolver, channelServiceConfig.getSslKey(), "SslKey");
            groupChannelConnectionsConfig.setSslKey(
                    resolver.getResource(channelServiceConfig.getSslKey()));
        }

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
        return service;
    }

    /**
     * Initialize the web3j service
     *
     * @param channelServiceConfig
     * @return Web3J object
     * @throws Exception
     */
    public static Web3j initWeb3j(
            BCOSStubConfig.ChannelService channelServiceConfig, Service service) throws Exception {
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        channelEthereumService.setTimeout(channelServiceConfig.getTimeout());
        return org.fisco.bcos.web3j.protocol.Web3j.build(
                channelEthereumService, service.getGroupId());
    }

    public static void checkCertExist(
            PathMatchingResourcePatternResolver resolver, String location, String key)
            throws WeCrossException {
        if (!resolver.getResource(location).exists()) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.DIR_NOT_EXISTS,
                    key + " does not exist, please check.");
        }
    }
}
