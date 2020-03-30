package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.common.BCOSConstant;
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

public class Web3jUtility {

    private static final Logger logger = LoggerFactory.getLogger(Web3jUtility.class);

    private Web3jUtility() {}
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

        EncryptType encryptType =
                new EncryptType(
                        channelServiceConfig.getChain().isEnableGM()
                                ? EncryptType.SM2_TYPE
                                : EncryptType.ECDSA_TYPE);
        logger.trace(" EncryptType: {}", encryptType.getEncryptType());

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

        /** Initialize the Web3J service */
        service.setConnectSeconds(BCOSConstant.WE3J_START_TIMEOUT_DEFAULT);
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
