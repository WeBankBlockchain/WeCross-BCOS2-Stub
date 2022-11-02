package com.webank.wecross.stub.bcos.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.bcos.client.ClientDefaultConfig;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSToml;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        Map<String, Object> commonConfigValue = (Map<String, Object>) stubConfig.get("common");
        requireItemNotNull(commonConfigValue, "common", getConfigPath());

        String stubName = (String) commonConfigValue.get("name");
        requireFieldNotNull(stubName, "common", "name", getConfigPath());

        String stubType = (String) commonConfigValue.get("type");
        requireFieldNotNull(stubType, "common", "type", getConfigPath());

        Map<String, Object> chainConfigValue = (Map<String, Object>) stubConfig.get("chain");
        requireItemNotNull(chainConfigValue, "chain", getConfigPath());
        BCOSStubConfig.Chain chain = getChainConfig(chainConfigValue);

        Map<String, Object> channelServiceConfigValue =
                (Map<String, Object>) stubConfig.get("channelService");
        requireItemNotNull(channelServiceConfigValue, "channelService", getConfigPath());
        BCOSStubConfig.ChannelService channelServiceConfig =
                getChannelServiceConfig(getConfigPath(), channelServiceConfigValue);

        List<Map<String, String>> resourcesConfigValue =
                (List<Map<String, String>>) stubConfig.get("resources");

        if (resourcesConfigValue == null) {
            resourcesConfigValue = new ArrayList<>();
        }

        List<BCOSStubConfig.Resource> bcosResources =
                getBCOSResourceConfig(getConfigPath(), chain, resourcesConfigValue);

        BCOSStubConfig bcosStubConfig = new BCOSStubConfig();
        bcosStubConfig.setType(stubType);
        bcosStubConfig.setChannelService(channelServiceConfig);
        bcosStubConfig.setResources(bcosResources);
        bcosStubConfig.setChain(chain);
        channelServiceConfig.setChain(chain);

        return bcosStubConfig;
    }

    public BCOSStubConfig.Chain getChainConfig(Map<String, Object> chainConfigValue) {
        // groupId field
        Long groupId = (Long) chainConfigValue.get("groupId");
        // chain field
        Long chainId = (Long) chainConfigValue.get("chainId");

        BCOSStubConfig.Chain chain = new BCOSStubConfig.Chain();
        chain.setChainID(
                Objects.nonNull(chainId)
                        ? chainId.intValue()
                        : ClientDefaultConfig.DEFAULT_CHAIN_ID);
        chain.setGroupID(
                Objects.nonNull(groupId)
                        ? groupId.intValue()
                        : ClientDefaultConfig.DEFAULT_GROUP_ID);

        return chain;
    }

    public BCOSStubConfig.ChannelService getChannelServiceConfig(
            String configFile, Map<String, Object> channelServiceConfigValue) {
        // timeout field
        Long timeout = (Long) channelServiceConfigValue.get("timeout");
        // thread num
        Long threadNum = (Long) channelServiceConfigValue.get("threadNum");
        // thread threadQueueCapacity
        Long threadQueueCapacity = (Long) channelServiceConfigValue.get("threadQueueCapacity");

        // caCert field
        String caCertPath =
                stubDir + File.separator + (String) channelServiceConfigValue.get("caCert");
        requireFieldNotNull(caCertPath, "channelService", "caCert", configFile);

        // sslCert field
        String sslCert =
                stubDir + File.separator + (String) channelServiceConfigValue.get("sslCert");
        requireFieldNotNull(sslCert, "channelService", "sslCert", configFile);

        // sslKey field
        String sslKey = stubDir + File.separator + (String) channelServiceConfigValue.get("sslKey");
        requireFieldNotNull(sslKey, "channelService", "sslKey", configFile);

        boolean gmConnectEnable =
                channelServiceConfigValue.get("gmConnectEnable") != null
                        && (boolean) channelServiceConfigValue.get("gmConnectEnable");

        // connectionsStr field
        @SuppressWarnings("unchecked")
        List<String> connectionsStr =
                (List<String>) channelServiceConfigValue.get("connectionsStr");
        requireFieldNotNull(connectionsStr, "channelService", "connectionsStr", configFile);

        BCOSStubConfig.ChannelService channelServiceConfig = new BCOSStubConfig.ChannelService();
        channelServiceConfig.setTimeout(
                Objects.isNull(timeout)
                        ? ClientDefaultConfig.CHANNEL_SERVICE_DEFAULT_TIMEOUT
                        : timeout.intValue());

        channelServiceConfig.setThreadNum(
                Objects.isNull(threadNum)
                        ? ClientDefaultConfig.CHANNEL_SERVICE_DEFAULT_THREAD_NUMBER
                        : threadNum.intValue());

        channelServiceConfig.setQueueCapacity(
                Objects.isNull(threadQueueCapacity)
                        ? ClientDefaultConfig.CHANNEL_SERVICE_DEFAULT_THREAD_QUEUE_CAPACITY
                        : threadQueueCapacity.intValue());

        channelServiceConfig.setCaCert(caCertPath);
        channelServiceConfig.setSslCert(sslCert);
        channelServiceConfig.setSslKey(sslKey);
        channelServiceConfig.setGmConnectEnable(gmConnectEnable);

        if (gmConnectEnable) {
            String gmCaCert =
                    stubDir + File.separator + (String) channelServiceConfigValue.get("gmCaCert");
            requireFieldNotNull(gmCaCert, "channelService", "gmCaCert", configFile);

            String gmSslCert =
                    stubDir + File.separator + (String) channelServiceConfigValue.get("gmSslCert");
            requireFieldNotNull(gmSslCert, "channelService", "gmSslCert", configFile);

            String gmSslKey =
                    stubDir + File.separator + (String) channelServiceConfigValue.get("gmSslKey");
            requireFieldNotNull(gmSslKey, "channelService", "gmSslKey", configFile);

            String gmEnSslCert =
                    stubDir
                            + File.separator
                            + (String) channelServiceConfigValue.get("gmEnSslCert");
            requireFieldNotNull(gmEnSslCert, "channelService", "gmEnSslCert", configFile);

            String gmEnSslKey =
                    stubDir + File.separator + (String) channelServiceConfigValue.get("gmEnSslKey");
            requireFieldNotNull(gmEnSslKey, "channelService", "gmEnSslKey", configFile);

            channelServiceConfig.setGmCaCert(gmCaCert);
            channelServiceConfig.setGmSslCert(gmSslCert);
            channelServiceConfig.setGmSslKey(gmSslKey);
            channelServiceConfig.setGmEnSslCert(gmEnSslCert);
            channelServiceConfig.setGmEnSslKey(gmEnSslKey);
        }
        channelServiceConfig.setConnectionsStr(connectionsStr);

        logger.debug(" ChannelServiceConfig: {}", channelServiceConfig);

        return channelServiceConfig;
    }

    public List<BCOSStubConfig.Resource> getBCOSResourceConfig(
            String configFile,
            BCOSStubConfig.Chain chain,
            List<Map<String, String>> resourcesConfigValue) {
        List<BCOSStubConfig.Resource> resourceList = new ArrayList<>();

        for (Map<String, String> stringStringMap : resourcesConfigValue) {
            String name = stringStringMap.get("name");
            requireFieldNotNull(name, "resources", "name", configFile);

            String type = stringStringMap.get("type");
            requireFieldNotNull(type, "resources", "type", configFile);
            // check type invalid
            if (!BCOSConstant.RESOURCE_TYPE_BCOS_CONTRACT.equals(type)) {
                logger.error(" unrecognized bcos resource type, name: {}, type: {}", name, type);
                continue;
            }

            String address = stringStringMap.get("contractAddress");
            requireFieldNotNull(address, "resources", "contractAddress", configFile);

            BCOSStubConfig.Resource resource = new BCOSStubConfig.Resource();
            resource.setName(name);
            resource.setType(type);
            resource.setValue(address);
            resource.setChain(chain);

            resourceList.add(resource);
        }

        logger.debug("resources: {}", resourceList);

        return resourceList;
    }
}
