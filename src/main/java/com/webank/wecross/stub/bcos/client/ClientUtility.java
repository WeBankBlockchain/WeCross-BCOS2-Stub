package com.webank.wecross.stub.bcos.client;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.config.model.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ClientUtility {

    private static final Logger logger = LoggerFactory.getLogger(ClientUtility.class);

    public static Client initClient(BCOSStubConfig bcosStubConfig) throws Exception {
        BCOSStubConfig.ChainRpcService chainRpcServiceConfig = bcosStubConfig.getChainRpcService();

        // groupID
        String groupID = chainRpcServiceConfig.getChain().getGroupID();
        // cryptoMaterial
        Map<String, Object> cryptoMaterial = buildCryptoMaterial(chainRpcServiceConfig);

        // network
        Map<String, Object> network = new HashMap<>();
        network.put("peers", chainRpcServiceConfig.getConnectionsStr());

        // threadPool
        Map<String, Object> threadPool = new HashMap<>();
        threadPool.put(
                "channelProcessorThreadSize", String.valueOf(chainRpcServiceConfig.getThreadNum()));
        threadPool.put(
                "receiptProcessorThreadSize", String.valueOf(chainRpcServiceConfig.getThreadNum()));
        threadPool.put(
                "maxBlockingQueueSize", String.valueOf(chainRpcServiceConfig.getQueueCapacity()));

        // configProperty
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setCryptoMaterial(cryptoMaterial);
        configProperty.setNetwork(network);
        configProperty.setThreadPool(threadPool);

        // configOption
        ConfigOption configOption = new ConfigOption(configProperty);

        // bcosSDK
        BcosSDK bcosSDK = new BcosSDK(configOption);
        return bcosSDK.getClient(groupID);
    }

    private static Map<String, Object> buildCryptoMaterial(
            BCOSStubConfig.ChainRpcService chainRpcServiceConfig)
            throws WeCrossException, IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Map<String, Object> cryptoMaterial = new HashMap<>();
        if (chainRpcServiceConfig.isGmConnectEnable()) {
            // gm ssl
            cryptoMaterial.put("useSMCrypto", "true");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, chainRpcServiceConfig.getGmCaCert(), "caCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, chainRpcServiceConfig.getGmSslCert(), "sslCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, chainRpcServiceConfig.getGmSslKey(), "sslKey");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, chainRpcServiceConfig.getGmEnSslCert(), "enSslCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, chainRpcServiceConfig.getGmEnSslKey(), "enSslKey");
        } else {
            cryptoMaterial.put("useSMCrypto", "false");
            // not gm ssl
            checkCertExistAndPut(
                    resolver, cryptoMaterial, chainRpcServiceConfig.getCaCert(), "caCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, chainRpcServiceConfig.getSslCert(), "sslCert");
            checkCertExistAndPut(
                    resolver, cryptoMaterial, chainRpcServiceConfig.getSslKey(), "sslKey");
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
                    key + " does not exist, please check location: " + certLocation);
        }
        cryptoMaterial.put(key, certResource.getFile().getPath());
    }
}
