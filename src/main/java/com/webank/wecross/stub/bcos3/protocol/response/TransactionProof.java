package com.webank.wecross.stub.bcos3.protocol.response;

import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;

public class TransactionProof {

    private JsonTransactionResponse transWithProof;
    private TransactionReceipt receiptWithProof;

    public TransactionProof() {}

    public TransactionProof(
            JsonTransactionResponse transWithProof, TransactionReceipt receiptWithProof) {
        this.transWithProof = transWithProof;
        this.receiptWithProof = receiptWithProof;
    }

    public JsonTransactionResponse getTransWithProof() {
        return transWithProof;
    }

    public void setTransWithProof(JsonTransactionResponse transWithProof) {
        this.transWithProof = transWithProof;
    }

    public TransactionReceipt getReceiptWithProof() {
        return receiptWithProof;
    }

    public void setReceiptWithProof(TransactionReceipt receiptWithProof) {
        this.receiptWithProof = receiptWithProof;
    }

    @Override
    public String toString() {
        return "TransactionProof{"
                + "transWithProof="
                + transWithProof
                + ", receiptWithProof="
                + receiptWithProof
                + '}';
    }
}
