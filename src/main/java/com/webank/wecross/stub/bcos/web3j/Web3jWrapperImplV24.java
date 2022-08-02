package com.webank.wecross.stub.bcos.web3j;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;

public class Web3jWrapperImplV24 extends Web3jWrapperImplV20 {

    public Web3jWrapperImplV24(Client client) {
        super(client);
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback){
        getClient().sendRawTransactionAndGetReceiptAsync(signedTransactionData, callback);
    }

    @Override
    public TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash){
        TransactionReceiptWithProof transactionReceiptWithProof =
                getClient().getTransactionReceiptByHashWithProof(transactionHash);
        return transactionReceiptWithProof.getResult();
    }

    @Override
    public TransactionWithProof.TransactionAndProof getTransactionByHashWithProof(String transactionHash){
        TransactionWithProof transactionWithProof =
                getClient().getTransactionByHashWithProof(transactionHash);
        return transactionWithProof.getResult();
    }
}
