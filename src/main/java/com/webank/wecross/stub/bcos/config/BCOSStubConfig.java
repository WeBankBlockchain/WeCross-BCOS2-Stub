package com.webank.wecross.stub.bcos.config;

import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
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
    /** stub type, BCOS */
    private String type;
    /** */
    private Chain chain;
    /** ChainRpcServiceConfig, used for JavaSDK initialize */
    private ChainRpcService chainRpcService;
    /** BCOS resource list */
    private List<Resource> resources;

    public boolean isGMStub() {
        return StringUtils.startsWithIgnoreCase(type, "gm");
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

    public static class ChainRpcService {
        private int timeout;
        private String caCert;
        private String sslCert;
        private String sslKey;
        private boolean gmConnectEnable;

        private String gmCaCert;
        private String gmSslCert;
        private String gmSslKey;
        private String gmEnSslCert;
        private String gmEnSslKey;
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

        public boolean isGmConnectEnable() {
            return gmConnectEnable;
        }

        public void setGmConnectEnable(boolean gmConnectEnable) {
            this.gmConnectEnable = gmConnectEnable;
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
            return "ChainRpcService{"
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

    public ChainRpcService getChainRpcService() {
        return chainRpcService;
    }

    public void setChannelService(ChainRpcService chainRpcService) {
        this.chainRpcService = chainRpcService;
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
                + ", chainRpcService="
                + chainRpcService
                + ", resourceConfig="
                + resources
                + '}';
    }

    public List<ResourceInfo> convertToResourceInfos() {
        List<ResourceInfo> resourceInfos = new ArrayList<>();
        Hash hash = new Keccak256();
        for (int i = 0; i < resources.size(); ++i) {
            ResourceInfo resourceInfo = new ResourceInfo();
            BCOSStubConfig.Resource resource = resources.get(i);

            resourceInfo.setName(resource.getName());
            resourceInfo.setStubType(this.type);
            resourceInfo.setChecksum(hash.hash(resource.getValue()));

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
