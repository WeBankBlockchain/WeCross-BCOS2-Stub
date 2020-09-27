package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Resolve the BCOS stub.toml to get BCOSConfig object */
public class BCOSStubConfig {
    private static Logger logger = LoggerFactory.getLogger(BCOSStubConfig.class);
    /** stub type, BCOS */
    private String type;
    /** */
    private Chain chain;
    /** ChannelServiceConfig, used for JavaSDK initialize */
    private ChannelService channelService;
    /** BCOS resource list */
    private List<Resource> resources;

    private Sealers sealers;

    public static class Chain {
        private int groupID;
        private int chainID;

        public int getGroupID() {
            return groupID;
        }

        public void setGroupID(int groupID) {
            this.groupID = groupID;
        }

        public int getChainID() {
            return chainID;
        }

        public void setChainID(int chainID) {
            this.chainID = chainID;
        }

        @Override
        public String toString() {
            return "Chain{" + "groupID=" + groupID + ", chainID=" + chainID + '}';
        }
    }

    public static class ChannelService {
        private int timeout;
        private String caCert;
        private String sslCert;
        private String sslKey;
        private List<String> connectionsStr;
        private Chain chain;
        private int threadNum;
        private int queueCapacity;

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
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

        public List<String> getConnectionsStr() {
            return connectionsStr;
        }

        public void setConnectionsStr(List<String> connectionsStr) {
            this.connectionsStr = connectionsStr;
        }

        public Chain getChain() {
            return chain;
        }

        public void setChain(Chain chain) {
            this.chain = chain;
        }

        public int getThreadNum() {
            return threadNum;
        }

        public void setThreadNum(int threadNum) {
            this.threadNum = threadNum;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        @Override
        public String toString() {
            return "ChannelService{"
                    + "timeout="
                    + timeout
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
                    + ", chain="
                    + chain
                    + ", threadNum="
                    + threadNum
                    + ", queueCapacity="
                    + queueCapacity
                    + '}';
        }
    }

    public static class Resource {
        private String name;
        private String type;
        private String value;
        private Chain chain;
        private Sealers sealers;

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

        public Chain getChain() {
            return chain;
        }

        public void setChain(Chain chain) {
            this.chain = chain;
        }

        public Sealers getSealers() {
            return sealers;
        }

        public void setSealers(Sealers sealers) {
            this.sealers = sealers;
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

    public static class Sealers {
        private Map<String, String> sealersMap;

        public Sealers(Map<String, String> sealersMap) {
            this.sealersMap = sealersMap;
        }

        public Map<String, String> getSealersMap() {
            return sealersMap;
        }

        public void setSealersMap(Map<String, String> sealersMap) {
            this.sealersMap = sealersMap;
        }

        @Override
        public String toString() {
            return "Sealers{" + "sealersMap=" + sealersMap + '}';
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

    public Sealers getSealers() {
        return sealers;
    }

    public void setSealers(Sealers sealers) {
        this.sealers = sealers;
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
        org.fisco.bcos.web3j.crypto.SHA3Digest sha3Digest =
                new org.fisco.bcos.web3j.crypto.SHA3Digest();
        for (int i = 0; i < resources.size(); ++i) {
            ResourceInfo resourceInfo = new ResourceInfo();
            BCOSStubConfig.Resource resource = resources.get(i);

            resourceInfo.setName(resource.getName());
            resourceInfo.setStubType(this.type);
            resourceInfo.setChecksum(sha3Digest.hash(resource.getValue()));

            resourceInfo.getProperties().put(resource.getName(), resource.getValue());
            resourceInfo
                    .getProperties()
                    .put(BCOSConstant.BCOS_GROUP_ID, resources.get(i).getChain().getGroupID());
            resourceInfo
                    .getProperties()
                    .put(BCOSConstant.BCOS_CHAIN_ID, resources.get(i).getChain().getChainID());

            resourceInfos.add(resourceInfo);
        }

        logger.info(" resource list: {}", resourceInfos);

        return resourceInfos;
    }
}
