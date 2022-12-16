package com.webank.wecross.stub.bcos3;

import com.webank.wecross.stub.bcos3.client.AbstractClientWrapper;
import com.webank.wecross.stub.bcos3.client.ClientWrapperFactory;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import com.webank.wecross.stub.bcos3.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos3.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos3.preparation.BfsServiceWrapper;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class BCOSConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(BCOSConnectionFactory.class);

    public static BCOSConnection build(String stubConfigPath, String configName) throws Exception {
        ScheduledExecutorService executorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("tmpBCOSConn-"));
        return build(stubConfigPath, configName, executorService);
    }

    public static BCOSConnection build(
            String stubConfigPath, String configName, ScheduledExecutorService executorService)
            throws Exception {
        logger.info(" stubConfigPath: {} ", stubConfigPath);

        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser(stubConfigPath, configName);
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        AbstractClientWrapper clientWrapper =
                ClientWrapperFactory.createClientWrapperInstance(bcosStubConfig);
        return build(bcosStubConfig, clientWrapper, executorService);
    }

    public static BCOSConnection build(
            BCOSStubConfig bcosStubConfig, AbstractClientWrapper clientWrapper) {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("tmpBCOSConn-"));
        return build(bcosStubConfig, clientWrapper, scheduledExecutorService);
    }

    public static BCOSConnection build(
            BCOSStubConfig bcosStubConfig,
            AbstractClientWrapper clientWrapper,
            ScheduledExecutorService executorService) {

        logger.info("bcosStubConfig: {}", bcosStubConfig);
        BCOSConnection bcosConnection = new BCOSConnection(clientWrapper, executorService);
        bcosConnection.setResourceInfoList(bcosStubConfig.convertToResourceInfos());

        bcosConnection.addProperty(
                BCOSConstant.BCOS_GROUP_ID, String.valueOf(bcosStubConfig.getChain().getGroupID()));
        bcosConnection.addProperty(
                BCOSConstant.BCOS_CHAIN_ID, String.valueOf(bcosStubConfig.getChain().getChainID()));
        bcosConnection.addProperty(
                BCOSConstant.BCOS_STUB_TYPE, String.valueOf(bcosStubConfig.getType()));

        BFSInfo proxyBFSInfo = BfsServiceWrapper.queryProxyBFSInfo(clientWrapper);
        if (Objects.nonNull(proxyBFSInfo)) {
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_NAME, proxyBFSInfo.getAddress());
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_ABI, proxyBFSInfo.getAbi());
        }

        BFSInfo hubBFSInfo = BfsServiceWrapper.queryHubBFSInfo(clientWrapper);
        if (Objects.nonNull(hubBFSInfo)) {
            bcosConnection.addProperty(BCOSConstant.BCOS_HUB_NAME, hubBFSInfo.getAddress());
        }
        return bcosConnection;
    }
}
