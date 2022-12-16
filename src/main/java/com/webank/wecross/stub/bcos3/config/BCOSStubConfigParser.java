package com.webank.wecross.stub.bcos3.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.bcos3.client.ClientDefaultConfig;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import com.webank.wecross.stub.bcos3.common.BCOSToml;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Load and parser stub.toml configuration file for BCOS */
public class BCOSStubConfigParser extends AbstractBCOSConfigParser {

    private static final Logger logger = LoggerFactory.getLogger(BCOSStubConfigParser.class);

    private final String stubDir;

    public BCOSStubConfigParser(String configPath, String configName) {
        super(configPath + File.separator + configName);
        this.stubDir = configPath;
    }

    /**
     * parser configPath file and return BCOSConfig object
     *
     * @return
     * @throws IOException
     */
    public BCOSStubConfig loadConfig() throws IOException {

        BCOSToml bcosToml = new BCOSToml(getConfigPath());
        Toml toml = bcosToml.getToml();

        Map<String, Object> stubConfig = toml.toMap();

        // common
        Map<String, Object> commonConfigValue = (Map<String, Object>) stubConfig.get("common");
        requireItemNotNull(commonConfigValue, "common", getConfigPath());
        String stubName = (String) commonConfigValue.get("name");
        requireFieldNotNull(stubName, "common", "name", getConfigPath());
        String stubType = (String) commonConfigValue.get("type");
        requireFieldNotNull(stubType, "common", "type", getConfigPath());

        // chain
        Map<String, Object> chainConfigValue = (Map<String, Object>) stubConfig.get("chain");
        requireItemNotNull(chainConfigValue, "chain", getConfigPath());
        BCOSStubConfig.Chain chainConfig = getChainConfig(chainConfigValue);

        // channelService
        Map<String, Object> channelServiceConfigValue =
                (Map<String, Object>) stubConfig.get("channelService");
        requireItemNotNull(channelServiceConfigValue, "channelService", getConfigPath());
        BCOSStubConfig.ChannelService channelServiceConfig =
                getChannelServiceConfig(getConfigPath(), channelServiceConfigValue, stubType);

        // resources
        List<Map<String, String>> resourcesConfigValue =
                (List<Map<String, String>>) stubConfig.get("resources");
        if (resourcesConfigValue == null) {
            resourcesConfigValue = new ArrayList<>();
        }
        List<BCOSStubConfig.Resource> resourcesConfig =
                getBCOSResourceConfig(getConfigPath(), chainConfig, resourcesConfigValue);

        BCOSStubConfig bcosStubConfig = new BCOSStubConfig();
        bcosStubConfig.setType(stubType);
        bcosStubConfig.setChain(chainConfig);
        bcosStubConfig.setChannelService(channelServiceConfig);
        bcosStubConfig.setResources(resourcesConfig);

        return bcosStubConfig;
    }

    public BCOSStubConfig.Chain getChainConfig(Map<String, Object> chainConfigValue) {
        // groupId field
        String groupId = (String) chainConfigValue.get("groupId");
        // chain field
        String chainId = (String) chainConfigValue.get("chainId");

        BCOSStubConfig.Chain chain = new BCOSStubConfig.Chain();
        chain.setChainID(
                StringUtils.isNotBlank(chainId) ? chainId : ClientDefaultConfig.DEFAULT_CHAIN_ID);
        chain.setGroupID(
                StringUtils.isNotBlank(groupId) ? groupId : ClientDefaultConfig.DEFAULT_GROUP_ID);

        return chain;
    }

    public BCOSStubConfig.ChannelService getChannelServiceConfig(
            String configFile, Map<String, Object> channelServiceConfigValue, String stubType) {
        // config
        BCOSStubConfig.ChannelService channelServiceConfig = new BCOSStubConfig.ChannelService();

        // caCert field
        String caCertPath = stubDir + File.separator + channelServiceConfigValue.get("caCert");
        requireFieldNotNull(caCertPath, "channelService", "caCert", configFile);
        // sslCert field
        String sslCert = stubDir + File.separator + channelServiceConfigValue.get("sslCert");
        requireFieldNotNull(sslCert, "channelService", "sslCert", configFile);
        // sslKey field
        String sslKey = stubDir + File.separator + channelServiceConfigValue.get("sslKey");
        requireFieldNotNull(sslKey, "channelService", "sslKey", configFile);
        channelServiceConfig.setCaCert(caCertPath);
        channelServiceConfig.setSslCert(sslCert);
        channelServiceConfig.setSslKey(sslKey);

        // stubType
        boolean isGmStub = StringUtils.containsIgnoreCase(stubType, BCOSConstant.GM);
        if (isGmStub) {
            String gmCaCert = stubDir + File.separator + channelServiceConfigValue.get("gmCaCert");
            requireFieldNotNull(gmCaCert, "channelService", "gmCaCert", configFile);

            String gmSslCert =
                    stubDir + File.separator + channelServiceConfigValue.get("gmSslCert");
            requireFieldNotNull(gmSslCert, "channelService", "gmSslCert", configFile);

            String gmSslKey = stubDir + File.separator + channelServiceConfigValue.get("gmSslKey");
            requireFieldNotNull(gmSslKey, "channelService", "gmSslKey", configFile);

            String gmEnSslCert =
                    stubDir + File.separator + channelServiceConfigValue.get("gmEnSslCert");
            requireFieldNotNull(gmEnSslCert, "channelService", "gmEnSslCert", configFile);

            String gmEnSslKey =
                    stubDir + File.separator + channelServiceConfigValue.get("gmEnSslKey");
            requireFieldNotNull(gmEnSslKey, "channelService", "gmEnSslKey", configFile);

            channelServiceConfig.setGmCaCert(gmCaCert);
            channelServiceConfig.setGmSslCert(gmSslCert);
            channelServiceConfig.setGmSslKey(gmSslKey);
            channelServiceConfig.setGmEnSslCert(gmEnSslCert);
            channelServiceConfig.setGmEnSslKey(gmEnSslKey);
        }

        // disableSsl
        Boolean disableSsl = (Boolean) channelServiceConfigValue.get("disableSsl");
        channelServiceConfig.setDisableSsl(
                Objects.isNull(disableSsl)
                        ? ClientDefaultConfig.CHANNEL_SERVICE_DEFAULT_DISABLE_SSL
                        : disableSsl);

        // timeout field
        Long messageTimeout = (Long) channelServiceConfigValue.get("messageTimeout");
        channelServiceConfig.setMessageTimeout(
                Objects.isNull(messageTimeout)
                        ? ClientDefaultConfig.CHANNEL_SERVICE_DEFAULT_TIMEOUT
                        : messageTimeout.intValue());

        // connectionsStr field
        List<String> connectionsStr =
                (List<String>) channelServiceConfigValue.get("connectionsStr");
        requireFieldNotNull(connectionsStr, "channelService", "connectionsStr", configFile);
        channelServiceConfig.setConnectionsStr(connectionsStr);

        // thread num
        Long threadPoolSize = (Long) channelServiceConfigValue.get("threadPoolSize");
        channelServiceConfig.setThreadPoolSize(
                Objects.isNull(threadPoolSize)
                        ? ClientDefaultConfig.CHANNEL_SERVICE_DEFAULT_THREAD_NUMBER
                        : threadPoolSize.intValue());
        logger.debug(" ChannelServiceConfig: {}", channelServiceConfig);

        return channelServiceConfig;
    }

    public List<BCOSStubConfig.Resource> getBCOSResourceConfig(
            String configFile,
            BCOSStubConfig.Chain chain,
            List<Map<String, String>> resourcesConfigValue) {
        List<BCOSStubConfig.Resource> resourceList = new ArrayList<>();

        for (Map<String, String> stringStringMap : resourcesConfigValue) {
            // name
            String name = stringStringMap.get("name");
            requireFieldNotNull(name, "resources", "name", configFile);

            // type
            String type = stringStringMap.get("type");
            requireFieldNotNull(type, "resources", "type", configFile);
            // check type invalid
            if (!BCOSConstant.RESOURCE_TYPE_BCOS_CONTRACT.equals(type)) {
                logger.error(" unrecognized bcos resource type, name: {}, type: {}", name, type);
                continue;
            }

            // contractAddress
            String address = stringStringMap.get("contractAddress");
            requireFieldNotNull(address, "resources", "contractAddress", configFile);

            BCOSStubConfig.Resource resource = new BCOSStubConfig.Resource();
            resource.setName(name);
            resource.setType(type);
            resource.setValue(address);
            resourceList.add(resource);
        }

        logger.debug("resources: {}", resourceList);
        return resourceList;
    }
}
