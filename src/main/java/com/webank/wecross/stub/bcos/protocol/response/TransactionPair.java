package com.webank.wecross.stub.bcos.protocol.response;

import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public class TransactionPair {
    private TransactionReceipt receipt;
    private Transaction transaction;

    public TransactionPair() {}

    public TransactionPair(Transaction transaction, TransactionReceipt receipt) {
        this.receipt = receipt;
        this.transaction = transaction;
    }

    public TransactionReceipt getReceipt() {
        return receipt;
    }

    public void setReceipt(TransactionReceipt receipt) {
        this.receipt = receipt;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public String toString() {
        return "TransactionPair{" + "receipt=" + receipt + ", transaction=" + transaction + '}';
    }
}
