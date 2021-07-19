package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof.ReceiptAndProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof;

public class Web3jWrapperImplV24 extends Web3jWrapperImplV20 {

    public Web3jWrapperImplV24(Web3j web3j) {
        super(web3j);
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionSucCallback callback)
            throws IOException {
        getWeb3j().sendRawTransactionAndGetProof(signedTransactionData, callback);
    }

    @Override
    public ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash)
            throws IOException {
        TransactionReceiptWithProof transactionReceiptWithProof =
                getWeb3j().getTransactionReceiptByHashWithProof(transactionHash).send();
        return transactionReceiptWithProof.getResult();
    }

    @Override
    public TransactionWithProof.TransAndProof getTransactionByHashWithProof(String transactionHash)
            throws IOException {
        TransactionWithProof transactionWithProof =
                getWeb3j().getTransactionByHashWithProof(transactionHash).send();
        return transactionWithProof.getResult();
    }
}
