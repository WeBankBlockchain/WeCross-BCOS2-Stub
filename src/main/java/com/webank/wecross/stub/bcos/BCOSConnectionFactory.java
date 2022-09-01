package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.bcos.client.AbstractClientWrapper;
import com.webank.wecross.stub.bcos.client.ClientWrapperFactory;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.preparation.CnsService;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class BCOSConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(BCOSConnectionFactory.class);


    public static BCOSConnection build(BCOSStubConfig bcosStubConfig,
                                       AbstractClientWrapper clientWrapper) {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("tmpBCOSConn-"));
        return build(bcosStubConfig, clientWrapper, scheduledExecutorService);
    }


    public static BCOSConnection build(BCOSStubConfig bcosStubConfig,
                                       AbstractClientWrapper clientWrapper,
                                       ScheduledExecutorService executorService) {

        logger.info(" bcosStubConfig: {}, version: {} ", bcosStubConfig, clientWrapper.getVersion());
        BCOSConnection bcosConnection = new BCOSConnection(clientWrapper, executorService);
        bcosConnection.setResourceInfoList(bcosStubConfig.convertToResourceInfos());

        bcosConnection.addProperty(
                BCOSConstant.BCOS_GROUP_ID, String.valueOf(bcosStubConfig.getChain().getGroupID()));
        bcosConnection.addProperty(
                BCOSConstant.BCOS_CHAIN_ID, String.valueOf(bcosStubConfig.getChain().getChainID()));
        bcosConnection.addProperty(
                BCOSConstant.BCOS_STUB_TYPE, String.valueOf(bcosStubConfig.getType()));
        if (clientWrapper.getVersion() != null) {
            bcosConnection.addProperty(BCOSConstant.BCOS_NODE_VERSION, clientWrapper.getVersion());
        }

        CnsInfo proxyCnsInfo = CnsService.queryProxyCnsInfo(clientWrapper);
        if (Objects.nonNull(proxyCnsInfo)) {
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_NAME, proxyCnsInfo.getAddress());
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_ABI, proxyCnsInfo.getAbi());
        }

        CnsInfo hubCnsInfo = CnsService.queryHubCnsInfo(clientWrapper);
        if (Objects.nonNull(hubCnsInfo)) {
            bcosConnection.addProperty(BCOSConstant.BCOS_HUB_NAME, hubCnsInfo.getAddress());
        }
        return bcosConnection;
    }

    public static BCOSConnection build(String stubConfigPath,
                                       String configName) throws Exception {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("tmpBCOSConn-"));
        return build(stubConfigPath, configName, scheduledExecutorService);
    }

    public static BCOSConnection build(String stubConfigPath,
                                       String configName,
                                       ScheduledExecutorService executorService) throws Exception {
        logger.info(" stubConfigPath: {} ", stubConfigPath);

        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser(stubConfigPath, configName);
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        AbstractClientWrapper clientWrapper =
                ClientWrapperFactory.createClientWrapperInstance(bcosStubConfig);

        return build(bcosStubConfig, clientWrapper, executorService);
    }
}
