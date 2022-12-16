package com.webank.wecross.stub.bcos3.client;

import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

public class ClientWrapperWithNullMock extends ClientWrapperImplMock {

    @Override
    public TransactionReceipt getTransactionReceiptByHashWithProof(String transactionHash) {
        return new TransactionReceipt();
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback) {
        TransactionReceipt receipt = new TransactionReceipt();
        callback.onResponse(receipt);
    }

    @Override
    public JsonTransactionResponse getTransactionByHashWithProof(String transactionHash) {
        return new JsonTransactionResponse();
    }
}
