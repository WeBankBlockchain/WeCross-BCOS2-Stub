package com.webank.wecross.stub.bcos.protocol.request;

import com.webank.wecross.stub.TransactionRequest;

public class TransactionParams {

    public TransactionParams() {}

    /** */
    public enum TP_YPE {
        SEND_TX_BY_PROXY,
        CALL_BY_PROXY,
        SEND_TX,
        CALL
    };

    public TransactionParams(TransactionRequest transactionRequest, String data, TP_YPE tp_ype) {
        this.transactionRequest = transactionRequest;
        this.data = data;
        this.tp_ype = tp_ype;
    }

    private TransactionRequest transactionRequest;
    private String data;
    private String from;
    private String to;
    private String abi;
    private TP_YPE tp_ype;

    public TransactionRequest getTransactionRequest() {
        return transactionRequest;
    }

    public void setTransactionRequest(TransactionRequest transactionRequest) {
        this.transactionRequest = transactionRequest;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public TP_YPE getTp_ype() {
        return tp_ype;
    }

    public void setTp_ype(TP_YPE tp_ype) {
        this.tp_ype = tp_ype;
    }

    @Override
    public String toString() {
        return "TransactionParams{"
                + "transactionRequest="
                + transactionRequest
                + ", data='"
                + data
                + '\''
                + ", from='"
                + from
                + '\''
                + ", to='"
                + to
                + '\''
                + ", abi='"
                + abi
                + '\''
                + ", tp_ype="
                + tp_ype
                + '}';
    }
}
