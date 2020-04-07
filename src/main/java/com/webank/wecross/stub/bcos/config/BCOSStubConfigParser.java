package com.webank.wecross.stub.bcos.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSToml;
import com.webank.wecross.stub.bcos.web3j.Web3jDefaultConfig;
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

    public BCOSStubConfigParser(String configPath) {
        super(configPath);
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

        String stubName = (String) commonConfigValue.get("stub");
        requireFieldNotNull(stubName, "common", "stub", getConfigPath());

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
        requireItemNotNull(resourcesConfigValue, "resources", getConfigPath());
        List<BCOSStubConfig.Resource> bcosResources =
                getBCOSResourceConfig(getConfigPath(), chain, resourcesConfigValue);

        BCOSStubConfig bcosStubConfig = new BCOSStubConfig();
        bcosStubConfig.setStub(stubName);
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
        // chainId field
        Boolean enableGM = (Boolean) chainConfigValue.get("enableGM");

        BCOSStubConfig.Chain chain = new BCOSStubConfig.Chain();
        chain.setEnableGM(!Objects.isNull(enableGM) ? enableGM : false);
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

        // caCert field
        String caCertPath = (String) channelServiceConfigValue.get("caCert");
        requireFieldNotNull(caCertPath, "channelService", "caCert", configFile);

        // sslCert field
        String sslCert = (String) channelServiceConfigValue.get("sslCert");
        requireFieldNotNull(sslCert, "channelService", "sslCert", configFile);

        // sslKey field
        String sslKey = (String) channelServiceConfigValue.get("sslKey");
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

        for (int i = 0; i < resourcesConfigValue.size(); ++i) {
            String name = resourcesConfigValue.get(i).get("name");
            requireFieldNotNull(name, "resources", "name", configFile);

            String type = resourcesConfigValue.get(i).get("type");
            requireFieldNotNull(name, "resources", "type", configFile);
            // check type invalid
            if (!BCOSConstant.RESOURCE_TYPE_BCOS_CONTRACT.equals(type)) {
                logger.error(" unrecognized bcos resource type, name: {}, type: {}", name, type);
                continue;
            }

            String address = resourcesConfigValue.get(i).get("contractAddress");
            requireFieldNotNull(name, "resources", "contractAddress", configFile);

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
