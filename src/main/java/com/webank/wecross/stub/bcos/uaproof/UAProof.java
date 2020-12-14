package com.webank.wecross.stub.bcos.uaproof;

public class UAProof {
    private String proof;

    private String uaPub;
    private String caPub;

    private String uaSig;
    private String caSig;

    private long timestamp;

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public String getUaPub() {
        return uaPub;
    }

    public void setUaPub(String uaPub) {
        this.uaPub = uaPub;
    }

    public String getCaPub() {
        return caPub;
    }

    public void setCaPub(String caPub) {
        this.caPub = caPub;
    }

    public String getUaSig() {
        return uaSig;
    }

    public void setUaSig(String uaSig) {
        this.uaSig = uaSig;
    }

    public String getCaSig() {
        return caSig;
    }

    public void setCaSig(String caSig) {
        this.caSig = caSig;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UAProof{"
                + "proof='"
                + proof
                + '\''
                + ", uaPub='"
                + uaPub
                + '\''
                + ", caPub='"
                + caPub
                + '\''
                + ", uaSig='"
                + uaSig
                + '\''
                + ", caSig='"
                + caSig
                + '\''
                + ", timestamp="
                + timestamp
                + '}';
    }
}
