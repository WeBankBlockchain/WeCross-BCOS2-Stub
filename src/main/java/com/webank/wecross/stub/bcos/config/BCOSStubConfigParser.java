package com.webank.wecross.stub.bcos.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.common.BCOSToml;
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

        Map<String, Object> channelServiceConfigValue =
                (Map<String, Object>) stubConfig.get("channelService");
        requireItemNotNull(channelServiceConfigValue, "channelService", getConfigPath());
        BCOSStubConfig.ChannelService channelServiceConfig =
                getChannelServiceConfig(getConfigPath(), channelServiceConfigValue);

        List<Map<String, String>> resourcesConfigValue =
                (List<Map<String, String>>) stubConfig.get("resources");
        requireItemNotNull(resourcesConfigValue, "resources", getConfigPath());
        List<BCOSStubConfig.Resource> bcosResources =
                getBCOSResourceConfig(getConfigPath(), resourcesConfigValue);

        BCOSStubConfig bcosStubConfig = new BCOSStubConfig();
        bcosStubConfig.setStub(stubName);
        bcosStubConfig.setType(stubType);
        bcosStubConfig.setChannelService(channelServiceConfig);
        bcosStubConfig.setResources(bcosResources);

        return bcosStubConfig;
    }

    public BCOSStubConfig.ChannelService getChannelServiceConfig(
            String configFile, Map<String, Object> channelServiceConfigValue) {
        // timeout field
        Long timeout = (Long) channelServiceConfigValue.get("timeout");

        // groupId field
        Long groupID = (Long) channelServiceConfigValue.get("groupId");
        requireFieldNotNull(groupID, "channelService", "groupId", configFile);

        // caCert field
        String caCertPath = (String) channelServiceConfigValue.get("caCert");
        requireFieldNotNull(groupID, "channelService", "caCert", configFile);

        // sslCert field
        String sslCert = (String) channelServiceConfigValue.get("sslCert");
        requireFieldNotNull(groupID, "channelService", "sslCert", configFile);

        // sslKey field
        String sslKey = (String) channelServiceConfigValue.get("sslKey");
        requireFieldNotNull(groupID, "channelService", "sslKey", configFile);

        // groupId field
        Long groupId = (Long) channelServiceConfigValue.get("groupId");
        requireFieldNotNull(groupID, "channelService", "groupId", configFile);

        // chainId field
        Long chainId = (Long) channelServiceConfigValue.get("chainId");
        requireFieldNotNull(groupID, "channelService", "chainId", configFile);

        // chainId field
        Boolean enableGM = (Boolean) channelServiceConfigValue.get("enableGM");

        // connectionsStr field
        @SuppressWarnings("unchecked")
        List<String> connectionsStr =
                (List<String>) channelServiceConfigValue.get("connectionsStr");
        requireFieldNotNull(groupID, "channelService", "connectionsStr", configFile);

        BCOSStubConfig.ChannelService channelServiceConfig = new BCOSStubConfig.ChannelService();
        channelServiceConfig.setTimeout(
                Objects.isNull(timeout)
                        ? BCOSConstant.CHANNELSERVICE_TIMEOUT_DEFAULT
                        : timeout.intValue());

        channelServiceConfig.setGroupID(groupID.intValue());
        channelServiceConfig.setCaCert(caCertPath);
        channelServiceConfig.setSslCert(sslCert);
        channelServiceConfig.setSslKey(sslKey);
        channelServiceConfig.setConnectionsStr(connectionsStr);
        channelServiceConfig.setGroupID(groupId.intValue());
        channelServiceConfig.setChainID(chainId.intValue());
        channelServiceConfig.setEnableGM(Objects.isNull(enableGM) ? false : enableGM);

        logger.debug(" ChannelServiceConfig: {}", channelServiceConfig);

        return channelServiceConfig;
    }

    public List<BCOSStubConfig.Resource> getBCOSResourceConfig(
            String configFile, List<Map<String, String>> resourcesConfigValue) {
        List<BCOSStubConfig.Resource> resourceList = new ArrayList<>();

        for (int i = 0; i < resourcesConfigValue.size(); ++i) {
            String name = resourcesConfigValue.get(i).get("name");
            requireFieldNotNull(name, "resources", "name", configFile);

            String type = resourcesConfigValue.get(i).get("type");
            requireFieldNotNull(name, "resources", "type", configFile);
            // check type invalid
            if (!BCOSConstant.RESOURCE_TYPE_BCOS_CONTRACT.equals(type)) {
                logger.warn("unkown bcos resource type, name: {}, type: {}", name, type);
            }

            String address = resourcesConfigValue.get(i).get("contractAddress");
            requireFieldNotNull(name, "resources", "contractAddress", configFile);

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
