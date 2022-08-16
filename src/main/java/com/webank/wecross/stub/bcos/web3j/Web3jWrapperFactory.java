package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.common.FeatureSupport;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import org.fisco.bcos.sdk.channel.model.EnumNodeVersion;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.model.NodeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3jWrapperFactory {

    private static final Logger logger = LoggerFactory.getLogger(Web3jWrapperFactory.class);

    public static AbstractWeb3jWrapper createWeb3jWrapperInstance(BCOSStubConfig bcosStubConfig)
            throws Exception {

        logger.info("BCOSStubConfig: {}", bcosStubConfig);

        Client client = Web3jUtility.initClient(bcosStubConfig.getChannelService());
        NodeVersion.ClientVersion nodeVersion = client.getNodeVersion().getNodeVersion();

        logger.info("NodeVersion: {}", nodeVersion);

        checkConfig(nodeVersion, bcosStubConfig.getType());

        EnumNodeVersion.Version version =
                EnumNodeVersion.getClassVersion(nodeVersion.getSupportedVersion());

        if (!FeatureSupport.isSupportVersion(version)) {
            throw new UnsupportedOperationException(
                    "Unsupported BCOS version, version: " + nodeVersion);
        }

        AbstractWeb3jWrapper web3jWrapper = createWeb3jWrapperInstance(version, client);
        web3jWrapper.setVersion(nodeVersion.getSupportedVersion());

        return web3jWrapper;
    }

    private static AbstractWeb3jWrapper createWeb3jWrapperInstance(
            EnumNodeVersion.Version version, Client client) {

        if (FeatureSupport.isSupportGetBlockHeader(version)) {
            logger.info("new Web3jWrapperImplV26");
            return new Web3jWrapperImplV26(client);
        } else if (FeatureSupport.isSupportGetTxProof(version)) {
            logger.info("new Web3jWrapperImplV24");
            return new Web3jWrapperImplV24(client);
        }

        logger.info("new Web3jWrapperImplV20");
        // default version
        return new Web3jWrapperImplV20(client);
    }

    private static void checkConfig(NodeVersion.ClientVersion version, String stubType) throws Exception {
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
}
