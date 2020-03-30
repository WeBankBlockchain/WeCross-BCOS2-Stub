package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Semaphore;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.fisco.bcos.web3j.protocol.core.methods.request.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosTransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public class Web3jWrapperImpl implements Web3jWrapper {

    private Web3j web3j;

    public Web3jWrapperImpl(Web3j web3j) {
        this.web3j = web3j;
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        BcosBlock bcosBlock =
                web3j.getBlockByNumber(
                                DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)),
                                false)
                        .send();
        return bcosBlock.getResult();
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        BlockNumber blockNumber = web3j.getBlockNumber().send();
        return blockNumber.getBlockNumber();
    }

    @Override
    public TransactionReceipt getTransactionReceipt(String hash) throws IOException {
        BcosTransactionReceipt bcosTransactionReceipt = web3j.getTransactionReceipt(hash).send();
        return bcosTransactionReceipt.getResult();
    }

    @Override
    public Call.CallOutput call(String contractAddress, String data) throws IOException {
        Call ethCall =
                web3j.call(
                                Transaction.createEthCallTransaction(
                                        contractAddress, contractAddress, data),
                                DefaultBlockParameterName.LATEST)
                        .send();
        return ethCall.getResult();
    }

    class Callback extends TransactionSucCallback {
        Callback() {
            try {
                semaphore.acquire(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void onResponse(TransactionReceipt receipt) {
            this.receipt = receipt;
            semaphore.release();
        }

        public TransactionReceipt receipt;
        public Semaphore semaphore = new Semaphore(1, true);
    }

    @Override
    public TransactionReceipt sendTransaction(String signTx) throws IOException {

        Callback callback = new Callback();
        try {
            web3j.sendRawTransaction(signTx, callback);
            callback.semaphore.acquire(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return callback.receipt;
    }
}
