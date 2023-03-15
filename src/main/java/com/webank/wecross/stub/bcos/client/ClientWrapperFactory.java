package com.webank.wecross.stub.bcos.client;

import com.webank.wecross.stub.bcos.common.FeatureSupport;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import org.fisco.bcos.sdk.channel.model.EnumNodeVersion;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.model.NodeVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientWrapperFactory {

    private static final Logger logger = LoggerFactory.getLogger(ClientWrapperFactory.class);

    public static AbstractClientWrapper createClientWrapperInstance(BCOSStubConfig bcosStubConfig)
            throws Exception {

        logger.info("BCOSStubConfig: {}", bcosStubConfig);

        Client client = ClientUtility.initClient(bcosStubConfig);
        NodeVersion.ClientVersion nodeVersion = client.getNodeVersion().getNodeVersion();

        logger.info("NodeVersion: {}", nodeVersion);

        checkConfig(nodeVersion, bcosStubConfig.getType());

        EnumNodeVersion.Version version =
                EnumNodeVersion.getClassVersion(nodeVersion.getSupportedVersion());

        if (!FeatureSupport.isSupportVersion(version)) {
            throw new UnsupportedOperationException(
                    "Unsupported BCOS version, version: " + nodeVersion);
        }

        AbstractClientWrapper clientWrapper = createClientWrapperInstance(version, client);
        clientWrapper.setVersion(nodeVersion.getSupportedVersion());

        return clientWrapper;
    }

    private static AbstractClientWrapper createClientWrapperInstance(
            EnumNodeVersion.Version version, Client client) {

        if (FeatureSupport.isSupportGetBlockHeader(version)) {
            logger.info("new ClientWrapperImplV26");
            return new ClientWrapperImplV26(client);
        } else if (FeatureSupport.isSupportGetTxProof(version)) {
            logger.info("new ClientWrapperImplV24");
            return new ClientWrapperImplV24(client);
        }

        logger.info("new ClientWrapperImplV20");
        // default version
        return new ClientWrapperImplV20(client);
    }

    private static void checkConfig(NodeVersion.ClientVersion version, String stubType)
            throws Exception {
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
