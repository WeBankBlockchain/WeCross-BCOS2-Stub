package com.webank.wecross.stub.bcos.protocol.response;

import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;

public class TransactionProof {

    private TransactionWithProof.TransactionAndProof transAndProof;
    private TransactionReceiptWithProof.ReceiptAndProof receiptAndProof;

    public TransactionProof() {
    }

    public TransactionProof(
            TransactionWithProof.TransactionAndProof transAndProof,
            TransactionReceiptWithProof.ReceiptAndProof receiptAndProof) {
        this.transAndProof = transAndProof;
        this.receiptAndProof = receiptAndProof;
    }

    public void setTransAndProof(TransactionWithProof.TransactionAndProof transAndProof) {
        this.transAndProof = transAndProof;
    }

    public void setReceiptAndProof(TransactionReceiptWithProof.ReceiptAndProof receiptAndProof) {
        this.receiptAndProof = receiptAndProof;
    }

    public TransactionWithProof.TransactionAndProof getTransAndProof() {
        return transAndProof;
    }

    public TransactionReceiptWithProof.ReceiptAndProof getReceiptAndProof() {
        return receiptAndProof;
    }

    @Override
    public String toString() {
        return "TransactionProof{"
                + "transAndProof="
                + transAndProof
                + ", receiptAndProof="
                + receiptAndProof
                + '}';
    }
}
