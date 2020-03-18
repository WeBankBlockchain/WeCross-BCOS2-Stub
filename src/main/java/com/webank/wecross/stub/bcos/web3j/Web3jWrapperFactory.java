package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class Web3jWrapperFactory {

    private static Logger logger = LoggerFactory.getLogger(Web3jWrapperFactory.class);

    /** start JavaSDK service */
    public static Web3jWrapper build(BCOSStubConfig.ChannelService channelServiceConfig)
            throws Exception {

        EncryptType encryptType =
                new EncryptType(
                        channelServiceConfig.isEnableGM()
                                ? EncryptType.SM2_TYPE
                                : EncryptType.ECDSA_TYPE);

        logger.info(" encryptType:{}", encryptType.getEncryptType());

        Service service = new Service();
        service.setConnectSeconds(BCOSConstant.WE3J_START_TIMEOUT_DEFAULT);
        service.setGroupId(channelServiceConfig.getGroupID());

        List<ChannelConnections> allChannelConnections = new ArrayList<>();
        ChannelConnections channelConnections = new ChannelConnections();
        channelConnections.setGroupId(channelServiceConfig.getGroupID());
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

        service.setAllChannelConnections(groupChannelConnectionsConfig);

        // start JavaSDK service
        service.run();

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        channelEthereumService.setTimeout(channelServiceConfig.getTimeout());
        org.fisco.bcos.web3j.protocol.Web3j web3j =
                org.fisco.bcos.web3j.protocol.Web3j.build(
                        channelEthereumService, service.getGroupId());

        return new Web3jWrapperImpl(web3j, service);
    }
}
