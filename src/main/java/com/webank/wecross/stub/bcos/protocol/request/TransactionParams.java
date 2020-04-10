package com.webank.wecross.stub.bcos.protocol.request;

import com.webank.wecross.stub.TransactionRequest;

public class TransactionParams {

    public TransactionParams() {}

    public TransactionParams(
            TransactionRequest transactionRequest, String data, String from, String to) {
        this.transactionRequest = transactionRequest;
        this.data = data;
        this.from = from;
        this.to = to;
    }

    public TransactionParams(TransactionRequest transactionRequest, String data) {
        this(transactionRequest, data, null, null);
    }

    private TransactionRequest transactionRequest;
    private String data;
    private String from;
    private String to;

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
                + '}';
    }
}
