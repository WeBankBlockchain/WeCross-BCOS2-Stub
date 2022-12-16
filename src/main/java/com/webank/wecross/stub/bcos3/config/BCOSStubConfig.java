package com.webank.wecross.stub.bcos3.config;

import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.bcos3.common.BCOSConstant;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.v3.crypto.hash.Hash;
import org.fisco.bcos.sdk.v3.crypto.hash.Keccak256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Resolve the BCOS stub.toml to get BCOSConfig object */
public class BCOSStubConfig {
    private static Logger logger = LoggerFactory.getLogger(BCOSStubConfig.class);
    /** stub type, BCOS3_ECDSA_EVM、BCOS3_ECDSA_WASM、BCOS3_GM_EVM、BCOS3_GM_WASM */
    private String type;
    /** chain */
    private Chain chain;
    /** channelService, used for JavaSDK initialize */
    private ChannelService channelService;
    /** BCOS resource list */
    private List<Resource> resources;

    public boolean isGMStub() {
        return StringUtils.containsIgnoreCase(type, BCOSConstant.GM);
    }

    public boolean isWASMStub() {
        return StringUtils.containsIgnoreCase(type, BCOSConstant.WASM);
    }

    public static class Chain {
        private String groupID;
        private String chainID;

        public String getGroupID() {
            return groupID;
        }

        public void setGroupID(String groupID) {
            this.groupID = groupID;
        }

        public String getChainID() {
            return chainID;
        }

        public void setChainID(String chainID) {
            this.chainID = chainID;
        }

        @Override
        public String toString() {
            return "Chain{" + "groupID=" + groupID + ", chainID=" + chainID + '}';
        }
    }

    public static class ChannelService {

        private String caCert;
        private String sslCert;
        private String sslKey;

        private String gmCaCert;
        private String gmSslCert;
        private String gmSslKey;
        private String gmEnSslCert;
        private String gmEnSslKey;

        private boolean disableSsl;
        private int messageTimeout;
        private List<String> connectionsStr;

        private int threadPoolSize;

        public boolean isDisableSsl() {
            return disableSsl;
        }

        public void setDisableSsl(boolean disableSsl) {
            this.disableSsl = disableSsl;
        }

        public int getMessageTimeout() {
            return messageTimeout;
        }

        public void setMessageTimeout(int messageTimeout) {
            this.messageTimeout = messageTimeout;
        }

        public String getCaCert() {
            return caCert;
        }

        public void setCaCert(String caCert) {
            this.caCert = caCert;
        }

        public String getSslCert() {
            return sslCert;
        }

        public void setSslCert(String sslCert) {
            this.sslCert = sslCert;
        }

        public String getSslKey() {
            return sslKey;
        }

        public void setSslKey(String sslKey) {
            this.sslKey = sslKey;
        }

        public String getGmCaCert() {
            return gmCaCert;
        }

        public void setGmCaCert(String gmCaCert) {
            this.gmCaCert = gmCaCert;
        }

        public String getGmSslCert() {
            return gmSslCert;
        }

        public void setGmSslCert(String gmSslCert) {
            this.gmSslCert = gmSslCert;
        }

        public String getGmSslKey() {
            return gmSslKey;
        }

        public void setGmSslKey(String gmSslKey) {
            this.gmSslKey = gmSslKey;
        }

        public String getGmEnSslCert() {
            return gmEnSslCert;
        }

        public void setGmEnSslCert(String gmEnSslCert) {
            this.gmEnSslCert = gmEnSslCert;
        }

        public String getGmEnSslKey() {
            return gmEnSslKey;
        }

        public void setGmEnSslKey(String gmEnSslKey) {
            this.gmEnSslKey = gmEnSslKey;
        }

        public List<String> getConnectionsStr() {
            return connectionsStr;
        }

        public void setConnectionsStr(List<String> connectionsStr) {
            this.connectionsStr = connectionsStr;
        }

        public int getThreadPoolSize() {
            return threadPoolSize;
        }

        public void setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
        }

        @Override
        public String toString() {
            return "ChannelService{"
                    + "messageTimeout="
                    + messageTimeout
                    + ", caCert='"
                    + caCert
                    + '\''
                    + ", sslCert='"
                    + sslCert
                    + '\''
                    + ", sslKey='"
                    + sslKey
                    + '\''
                    + ", connectionsStr="
                    + connectionsStr
                    + ", threadPoolSize="
                    + threadPoolSize
                    + '}';
        }
    }

    public static class Resource {
        private String name;
        private String type;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "BCOSResourceConfig{"
                    + "name='"
                    + name
                    + '\''
                    + ", type='"
                    + type
                    + '\''
                    + ", value='"
                    + value
                    + '\''
                    + '}';
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ChannelService getChannelService() {
        return channelService;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    @Override
    public String toString() {
        return "BCOSConfig{"
                + "type='"
                + type
                + '\''
                + ", channelService="
                + channelService
                + ", resourceConfig="
                + resources
                + '}';
    }

    public List<ResourceInfo> convertToResourceInfos() {
        List<ResourceInfo> resourceInfos = new ArrayList<>();
        Hash hash = new Keccak256();
        for (Resource resource : resources) {
            ResourceInfo resourceInfo = new ResourceInfo();
            resourceInfo.setName(resource.getName());
            resourceInfo.setStubType(this.type);
            resourceInfo.setChecksum(hash.hash(resource.getValue()));
            resourceInfo.getProperties().put(resource.getName(), resource.getValue());
            resourceInfo.getProperties().put(BCOSConstant.BCOS_GROUP_ID, this.chain.getGroupID());
            resourceInfo.getProperties().put(BCOSConstant.BCOS_CHAIN_ID, this.chain.getChainID());
            resourceInfos.add(resourceInfo);
        }

        logger.info(" resource list: {}", resourceInfos);

        return resourceInfos;
    }
}
