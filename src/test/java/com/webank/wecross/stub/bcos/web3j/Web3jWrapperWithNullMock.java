package com.webank.wecross.stub.bcos.web3j;

import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;

public class Web3jWrapperWithNullMock extends Web3jWrapperImplMock {

    @Override
    public TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash) {
        TransactionReceiptWithProof.ReceiptAndProof receiptAndProof =
                new TransactionReceiptWithProof.ReceiptAndProof();
        return receiptAndProof;
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback) {
        TransactionReceipt receipt = new TransactionReceipt();
        callback.onResponse(receipt);
    }

    @Override
    public TransactionWithProof.TransactionAndProof getTransactionByHashWithProof(String transactionHash) {
        TransactionWithProof.TransactionAndProof transAndProof = new TransactionWithProof.TransactionAndProof();
        return transAndProof;
    }
}
