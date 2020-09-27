package com.webank.wecross.stub.bcos.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSToml;
import com.webank.wecross.stub.bcos.web3j.Web3jDefaultConfig;
import java.io.File;
import java.io.IOException;
import java.util.*;
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

        List<Map<String, String>> sealersConfigValue =
                (List<Map<String, String>>) stubConfig.get("sealers");

        if (resourcesConfigValue == null) {
            resourcesConfigValue = new ArrayList<>();
        }
        if (sealersConfigValue == null) {
            logger.error("loadConfig: Can't get sealers in config file!");
            throw new IOException("loadConfig: Can't get sealers in config file!");
        }

        List<BCOSStubConfig.Resource> bcosResources =
                getBCOSResourceConfig(getConfigPath(), chain, resourcesConfigValue);

        BCOSStubConfig.Sealers sealers = getBCOSSealersConfig(getConfigPath(), sealersConfigValue);

        BCOSStubConfig bcosStubConfig = new BCOSStubConfig();
        bcosStubConfig.setType(stubType);
        bcosStubConfig.setChannelService(channelServiceConfig);
        bcosStubConfig.setResources(bcosResources);
        bcosStubConfig.setChain(chain);
        bcosStubConfig.setSealers(sealers);
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
                        : Web3jDefaultConfig.DEFAULT_CHAIN_ID);
        chain.setGroupID(
                Objects.nonNull(groupId)
                        ? groupId.intValue()
                        : Web3jDefaultConfig.DEFAULT_GROUP_ID);

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

        // connectionsStr field
        @SuppressWarnings("unchecked")
        List<String> connectionsStr =
                (List<String>) channelServiceConfigValue.get("connectionsStr");
        requireFieldNotNull(connectionsStr, "channelService", "connectionsStr", configFile);

        BCOSStubConfig.ChannelService channelServiceConfig = new BCOSStubConfig.ChannelService();
        channelServiceConfig.setTimeout(
                Objects.isNull(timeout)
                        ? Web3jDefaultConfig.CHANNEL_SERVICE_DEFAULT_TIMEOUT
                        : timeout.intValue());

        channelServiceConfig.setThreadNum(
                Objects.isNull(threadNum)
                        ? Web3jDefaultConfig.CHANNEL_SERVICE_DEFAULT_THREAD_NUMBER
                        : threadNum.intValue());

        channelServiceConfig.setQueueCapacity(
                Objects.isNull(threadQueueCapacity)
                        ? Web3jDefaultConfig.CHANNEL_SERVICE_DEFAULT_THREAD_QUEUE_CAPACITY
                        : threadQueueCapacity.intValue());

        channelServiceConfig.setCaCert(caCertPath);
        channelServiceConfig.setSslCert(sslCert);
        channelServiceConfig.setSslKey(sslKey);
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

    public BCOSStubConfig.Sealers getBCOSSealersConfig(
            String configFile, List<Map<String, String>> peersConfigValue) {
        Map<String, String> sealerMap = new HashMap<>();

        for (Map<String, String> peerConfigValue : peersConfigValue) {
            String id = peerConfigValue.get("id");
            requireFieldNotNull(id, "sealers", "id", configFile);

            String pubKey = peerConfigValue.get("pubKey");
            requireFieldNotNull(pubKey, "sealers", "pubKey", configFile);

            sealerMap.put(id, pubKey);
        }

        logger.debug("getBCOSPeerConfig: peers:{}", sealerMap);

        return new BCOSStubConfig.Sealers(sealerMap);
    }
}
