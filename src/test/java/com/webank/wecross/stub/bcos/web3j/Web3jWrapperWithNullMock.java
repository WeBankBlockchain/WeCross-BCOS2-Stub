package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof;

public class Web3jWrapperWithNullMock extends Web3jWrapperImplMock {
    @Override
    public TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(
            String transactionHash) throws IOException {
        TransactionReceiptWithProof.ReceiptAndProof receiptAndProof =
                new TransactionReceiptWithProof.ReceiptAndProof();
        return receiptAndProof;
    }

    @Override
    public TransactionReceipt sendTransactionAndGetProof(String signedTransactionData)
            throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        return receipt;
    }

    @Override
    public TransactionWithProof.TransAndProof getTransactionByHashWithProof(String transactionHash)
            throws IOException {
        TransactionWithProof.TransAndProof transAndProof = new TransactionWithProof.TransAndProof();
        return transAndProof;
    }
}
