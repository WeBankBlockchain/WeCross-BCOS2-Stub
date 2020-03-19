package com.webank.wecross.stub.bcos.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Resolve the BCOS stub.toml to get BCOSConfig object */
public class BCOSStubConfig {

    private static final Logger logger = LoggerFactory.getLogger(BCOSStubConfig.class);
    /** stub name */
    private String stub;
    /** stub type, BCOS */
    private String type;

    /** ChannelServiceConfig, used for JavaSDK initialize */
    private ChannelService channelService;
    /** BCOS resource list */
    private List<Resource> resources;

    public static class ChannelService {
        private int timeout;
        private String caCert;
        private String sslCert;
        private String sslKey;
        private int groupID;
        private boolean enableGM;
        private int chainID;
        private List<String> connectionsStr;

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

        public int getGroupID() {
            return groupID;
        }

        public void setGroupID(int groupID) {
            this.groupID = groupID;
        }

        public List<String> getConnectionsStr() {
            return connectionsStr;
        }

        public void setConnectionsStr(List<String> connectionsStr) {
            this.connectionsStr = connectionsStr;
        }

        public boolean isEnableGM() {
            return enableGM;
        }

        public void setEnableGM(boolean enableGM) {
            this.enableGM = enableGM;
        }

        public int getChainID() {
            return chainID;
        }

        public void setChainID(int chainID) {
            this.chainID = chainID;
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
                    + ", groupID="
                    + groupID
                    + ", enableGM="
                    + enableGM
                    + ", chainID="
                    + chainID
                    + ", connectionsStr="
                    + connectionsStr
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

    public String getStub() {
        return stub;
    }

    public void setStub(String stub) {
        this.stub = stub;
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

    @Override
    public String toString() {
        return "BCOSConfig{"
                + "stub='"
                + stub
                + '\''
                + ", type='"
                + type
                + '\''
                + ", channelService="
                + channelService
                + ", resourceConfig="
                + resources
                + '}';
    }
}
