package com.webank.wecross.stub.bcos.client;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.model.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ClientUtility {

    private static final Logger logger = LoggerFactory.getLogger(ClientUtility.class);

    public static Client initClient(BCOSStubConfig.ChannelService channelServiceConfig)
            throws Exception {
        // groupID
        int groupID = channelServiceConfig.getChain().getGroupID();
        // cryptoType
        int cryptoType =
                channelServiceConfig.isGmConnectEnable()
                        ? CryptoType.SM_TYPE
                        : CryptoType.ECDSA_TYPE;

        // cryptoMaterial
        Map<String, Object> cryptoMaterial = buildCryptoMaterial(channelServiceConfig);

        // network
        Map<String, Object> network = new HashMap<>();
        network.put("peers", channelServiceConfig.getConnectionsStr());

        // threadPool
        Map<String, Object> threadPool = new HashMap<>();
        threadPool.put(
                "channelProcessorThreadSize", String.valueOf(channelServiceConfig.getThreadNum()));
        threadPool.put(
                "receiptProcessorThreadSize", String.valueOf(channelServiceConfig.getThreadNum()));
        threadPool.put(
                "maxBlockingQueueSize", String.valueOf(channelServiceConfig.getQueueCapacity()));

        // configProperty
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setCryptoMaterial(cryptoMaterial);
        configProperty.setNetwork(network);
        configProperty.setThreadPool(threadPool);

        // configOption
        ConfigOption configOption = new ConfigOption(configProperty, cryptoType);

        // bcosSDK
        BcosSDK bcosSDK = new BcosSDK(configOption);
        return bcosSDK.getClient(groupID);
    }

    private static Map<String, Object> buildCryptoMaterial(
            BCOSStubConfig.ChannelService channelServiceConfig)
            throws WeCrossException, IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Map<String, Object> cryptoMaterial = new HashMap<>();
        if (channelServiceConfig.isGmConnectEnable()) {
            checkCertExistAndPut(
                    resolver, cryptoMaterial, channelServiceConfig.getGmCaCert(), "caCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, channelServiceConfig.getGmSslCert(), "sslCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, channelServiceConfig.getGmSslKey(), "sslKey");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, channelServiceConfig.getGmEnSslCert(), "enSslCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, channelServiceConfig.getGmEnSslKey(), "enSslKey");
        } else {
            checkCertExistAndPut(
                    resolver, cryptoMaterial, channelServiceConfig.getCaCert(), "caCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, channelServiceConfig.getSslCert(), "sslCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, channelServiceConfig.getSslKey(), "sslKey");
        }
        return cryptoMaterial;
    }

    private static void checkCertExistAndPut(
            PathMatchingResourcePatternResolver resolver,
            Map<String, Object> cryptoMaterial,
            String certLocation,
            String key)
            throws WeCrossException, IOException {
        Resource certResource = resolver.getResource(certLocation);
        if (!certResource.exists() || !certResource.isFile()) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.DIR_NOT_EXISTS,
                    key + " does not exist, please check.");
        }
        cryptoMaterial.put(key, certResource.getFile().getPath());
    }
}
