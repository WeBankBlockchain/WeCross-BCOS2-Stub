package com.webank.wecross.stub.bcos.uaproof;

public class PublicSign {
    private String proof;
    private String pubKey;
    private long timestamp;

    public PublicSign(String proof, String pubKey, long timestamp) {
        this.proof = proof;
        this.pubKey = pubKey;
        this.timestamp = timestamp;
    }

    public PublicSign(String pubKey, long timestamp) {
        this("", pubKey, timestamp);
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
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
