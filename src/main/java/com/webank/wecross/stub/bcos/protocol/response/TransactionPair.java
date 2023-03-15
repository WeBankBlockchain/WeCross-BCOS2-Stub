package com.webank.wecross.stub.bcos.protocol.response;

import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.model.TransactionReceipt;

public class TransactionPair {
    private TransactionReceipt receipt;
    private JsonTransactionResponse transaction;

    public TransactionPair() {}

    public TransactionPair(JsonTransactionResponse transaction, TransactionReceipt receipt) {
        this.receipt = receipt;
        this.transaction = transaction;
    }

    public TransactionReceipt getReceipt() {
        return receipt;
    }

    public void setReceipt(TransactionReceipt receipt) {
        this.receipt = receipt;
    }

    public JsonTransactionResponse getTransaction() {
        return transaction;
    }

    public void setTransaction(JsonTransactionResponse transaction) {
        this.transaction = transaction;
    }

    @Override
    public String toString() {
        return "TransactionPair{" + "receipt=" + receipt + ", transaction=" + transaction + '}';
    }
}
