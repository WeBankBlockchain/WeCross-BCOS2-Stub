package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.config.BCOSStubConfigParser;
import com.webank.wecross.stub.bcos.preparation.CnsService;
import com.webank.wecross.stub.bcos.web3j.Web3jUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImpl;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.fisco.bcos.web3j.precompile.cns.CnsInfo;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

public class BCOSConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(BCOSConnectionFactory.class);

    public static BCOSConnection build(BCOSStubConfig bcosStubConfig, Web3jWrapper web3jWrapper)
            throws Exception {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("tmpBCOSConn-"));
        return build(bcosStubConfig, web3jWrapper, scheduledExecutorService);
    }

    public static BCOSConnection build(
            BCOSStubConfig bcosStubConfig,
            Web3jWrapper web3jWrapper,
            ScheduledExecutorService executorService)
            throws Exception {
        /** load stub.toml config */
        logger.info(" stubConfigPath: {} ", bcosStubConfig);

        BCOSConnection bcosConnection = new BCOSConnection(web3jWrapper, executorService);
        bcosConnection.setResourceInfoList(bcosStubConfig.convertToResourceInfos());

        bcosConnection.addProperty(
                BCOSConstant.BCOS_GROUP_ID, String.valueOf(bcosStubConfig.getChain().getGroupID()));
        bcosConnection.addProperty(
                BCOSConstant.BCOS_CHAIN_ID, String.valueOf(bcosStubConfig.getChain().getChainID()));
        bcosConnection.addProperty(
                BCOSConstant.BCOS_STUB_TYPE, String.valueOf(bcosStubConfig.getType()));

        String sealerString =
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(bcosStubConfig.getSealers().getSealerList());
        bcosConnection.addProperty(BCOSConstant.BCOS_SEALER_LIST, sealerString);

        CnsInfo proxyCnsInfo = CnsService.queryProxyCnsInfo(web3jWrapper);
        if (Objects.nonNull(proxyCnsInfo)) {
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_NAME, proxyCnsInfo.getAddress());
            bcosConnection.addProperty(BCOSConstant.BCOS_PROXY_ABI, proxyCnsInfo.getAbi());
        }

        CnsInfo hubCnsInfo = CnsService.queryHubCnsInfo(web3jWrapper);
        if (Objects.nonNull(hubCnsInfo)) {
            bcosConnection.addProperty(BCOSConstant.BCOS_HUB_NAME, hubCnsInfo.getAddress());
        }
        return bcosConnection;
    }

    public static BCOSConnection build(String stubConfigPath, String configName) throws Exception {
        ScheduledExecutorService scheduledExecutorService =
                new ScheduledThreadPoolExecutor(4, new CustomizableThreadFactory("tmpBCOSConn-"));
        return build(stubConfigPath, configName, scheduledExecutorService);
    }

    public static BCOSConnection build(
            String stubConfigPath, String configName, ScheduledExecutorService executorService)
            throws Exception {
        /** load stub.toml config */
        logger.info(" stubConfigPath: {} ", stubConfigPath);
        BCOSStubConfigParser bcosStubConfigParser =
                new BCOSStubConfigParser(stubConfigPath, configName);
        BCOSStubConfig bcosStubConfig = bcosStubConfigParser.loadConfig();

        Web3j web3j = Web3jUtility.initWeb3j(bcosStubConfig);
        Web3jWrapper web3jWrapper = new Web3jWrapperImpl(web3j);
        logger.info(" web3j: {} ", web3j);

        return build(bcosStubConfig, web3jWrapper, executorService);
    }
}
