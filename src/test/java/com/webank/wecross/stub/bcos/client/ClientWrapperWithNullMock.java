package com.webank.wecross.stub.bcos.client;

import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

public class ClientWrapperWithNullMock extends ClientWrapperImplMock {

    @Override
    public TransactionReceipt getTransactionReceiptByHashWithProof(String transactionHash) {
        TransactionReceipt receiptAndProof = new TransactionReceipt();
        return receiptAndProof;
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback) {
        TransactionReceipt receipt = new TransactionReceipt();
        callback.onResponse(receipt);
    }

    @Override
    public JsonTransactionResponse getTransactionByHashWithProof(String transactionHash) {
        JsonTransactionResponse transAndProof = new JsonTransactionResponse();
        return transAndProof;
    }
}
