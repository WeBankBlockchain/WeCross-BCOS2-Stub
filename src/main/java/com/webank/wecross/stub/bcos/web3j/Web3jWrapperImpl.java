package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.request.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.*;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof.ReceiptAndProof;

public class Web3jWrapperImpl implements Web3jWrapper {

    private Web3j web3j;

    public Web3jWrapperImpl(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public Web3j getWeb3j() {
        return web3j;
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        BcosBlock bcosBlock = web3j.getBlockByNumber(BigInteger.valueOf(blockNumber), false).send();
        return bcosBlock.getResult();
    }

    @Override
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException {
        BcosBlockHeader bcosBlockHeader =
                web3j.getBlockHeaderByNumber(BigInteger.valueOf(blockNumber), true).send();
        return bcosBlockHeader.getBlockHeader();
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        BlockNumber blockNumber = web3j.getBlockNumber().send();
        return blockNumber.getBlockNumber();
    }

    @Override
    public ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash)
            throws IOException {
        TransactionReceiptWithProof transactionReceiptWithProof =
                web3j.getTransactionReceiptByHashWithProof(transactionHash).send();
        return transactionReceiptWithProof.getResult();
    }

    @Override
    public TransactionWithProof.TransAndProof getTransactionByHashWithProof(String transactionHash)
            throws IOException {
        TransactionWithProof transactionWithProof =
                web3j.getTransactionByHashWithProof(transactionHash).send();
        return transactionWithProof.getResult();
    }

    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, String data)
            throws IOException {
        Call ethCall =
                web3j.call(
                                Transaction.createEthCallTransaction(
                                        accountAddress, contractAddress, data))
                        .send();
        return ethCall.getResult();
    }

    @Override
    public void sendTransactionAndGetProof(
            String signedTransactionData, TransactionSucCallback callback) throws IOException {
        web3j.sendRawTransactionAndGetProof(signedTransactionData, callback);
    }
}
