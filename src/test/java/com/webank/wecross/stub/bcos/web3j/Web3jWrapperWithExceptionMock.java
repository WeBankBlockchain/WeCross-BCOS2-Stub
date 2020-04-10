package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof;

public class Web3jWrapperWithExceptionMock implements Web3jWrapper {

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public TransactionReceipt sendTransaction(String signedTransactionData) throws IOException {
        throw new IOException(" test IOException");
    }

    @Override
    public TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(
            String transactionHash) throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public TransactionWithProof.TransAndProof getTransactionByHashWithProof(String transactionHash)
            throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public Call.CallOutput call(String contractAddress, String data) throws IOException {
        throw new IOException(" IOException");
    }
}
