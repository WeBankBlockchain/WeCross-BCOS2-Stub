package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.FeatureSupport;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.fisco.EnumNodeVersion;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3jWrapperFactory {

    private static final Logger logger = LoggerFactory.getLogger(Web3jWrapperFactory.class);

    /**
     * @param bcosStubConfig
     * @return
     */
    public static AbstractWeb3jWrapper createWeb3jWrapperInstance(BCOSStubConfig bcosStubConfig)
            throws Exception {

        logger.info("BCOSStubConfig: {}", bcosStubConfig);
        Service service = Web3jUtility.initService(bcosStubConfig.getChannelService());
        Web3j web3j = Web3jUtility.initWeb3j(bcosStubConfig.getChannelService(), service);
        NodeVersion.Version nodeVersion = web3j.getNodeVersion().send().getNodeVersion();
        logger.info("NodeVersion: {}", nodeVersion);
        checkConfig(nodeVersion, bcosStubConfig.getType());

        EnumNodeVersion.Version version =
                EnumNodeVersion.getClassVersion(nodeVersion.getSupportedVersion());
        if (!FeatureSupport.isSupportVersion(version)) {
            throw new UnsupportedOperationException(
                    "Unsupported BCOS version, version: " + nodeVersion);
        }

        AbstractWeb3jWrapper web3jWrapper = createWeb3jWrapperInstance(version, web3j);
        web3jWrapper.setVersion(nodeVersion.getSupportedVersion());
        web3jWrapper.setService(service);

        return web3jWrapper;
    }

    /**
     * @param version
     * @parweb3j
     * @return
     */
    private static AbstractWeb3jWrapper createWeb3jWrapperInstance(
            EnumNodeVersion.Version version, Web3j web3j) {

        if (FeatureSupport.isSupportGetBlockHeader(version)) {
            logger.info("new Web3jWrapperImplV26");
            return new Web3jWrapperImplV26(web3j);
        } else if (FeatureSupport.isSupportGetTxProof(version)) {
            logger.info("new Web3jWrapperImplV24");
            return new Web3jWrapperImplV24(web3j);
        }

        logger.info("new Web3jWrapperImplV20");
        // default version
        return new Web3jWrapperImplV20(web3j);
    }

    /**
     * Check the stub config type
     *
     * @param version
     * @param stubType
     */
    private static void checkConfig(NodeVersion.Version version, String stubType) throws Exception {
        boolean isGMStub = stubType.toLowerCase().contains("gm");
        boolean isGMNode = version.getVersion().toLowerCase().contains("gm");

        if (logger.isDebugEnabled()) {
            logger.debug(" isGMStub: {}, isGMNode: {}", isGMStub, isGMNode);
        }

        if (!(isGMStub == isGMNode)) {
            throw new Exception(
                    "Please check config "
                            + "stub.toml common::type field, change to \""
                            + (isGMNode
                                    ? BCOSConstant.GM_BCOS_STUB_TYPE
                                    : BCOSConstant.BCOS_STUB_TYPE)
                            + "\"");
        }
    }
}
