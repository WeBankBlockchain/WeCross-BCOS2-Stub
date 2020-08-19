package com.webank.wecross.stub.bcos.protocol.response;

import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof;

public class TransactionProof {

    private TransactionWithProof.TransAndProof transAndProof;
    private TransactionReceiptWithProof.ReceiptAndProof receiptAndProof;

    public TransactionProof() {}

    public TransactionProof(
            TransactionWithProof.TransAndProof transAndProof,
            TransactionReceiptWithProof.ReceiptAndProof receiptAndProof) {
        this.transAndProof = transAndProof;
        this.receiptAndProof = receiptAndProof;
    }

    public void setTransAndProof(TransactionWithProof.TransAndProof transAndProof) {
        this.transAndProof = transAndProof;
    }

    public void setReceiptAndProof(TransactionReceiptWithProof.ReceiptAndProof receiptAndProof) {
        this.receiptAndProof = receiptAndProof;
    }

    public TransactionWithProof.TransAndProof getTransAndProof() {
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
