package com.webank.wecross.stub.bcos.uaproof;

public class PublicSign {
    private String pubKey;
    private long timestamp;

    public PublicSign(String proof, String pubKey, long timestamp) {
        this.pubKey = pubKey;
        this.timestamp = timestamp;
    }

    public PublicSign(String pubKey, long timestamp) {
        this("", pubKey, timestamp);
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
