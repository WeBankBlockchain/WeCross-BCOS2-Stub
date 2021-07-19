package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.*;

public class Web3jWrapperWithExceptionMock extends AbstractWeb3jWrapper {

    public Web3jWrapperWithExceptionMock() {
        super(null);
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        throw new IOException(" IOException");
    }

    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionSucCallback callback)
            throws IOException {
        throw new IOException(" IOException");
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
    public Call.CallOutput call(String accountAddress, String contractAddress, String data)
            throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public Web3j getWeb3j() {
        return new MockWeb3j();
    }
}
