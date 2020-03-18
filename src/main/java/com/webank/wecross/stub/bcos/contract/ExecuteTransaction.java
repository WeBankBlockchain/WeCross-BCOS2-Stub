package com.webank.wecross.stub.bcos.contract;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.Request;
import org.fisco.bcos.web3j.protocol.core.methods.response.SendTransaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public class ExecuteTransaction {

    private Web3j web3j;

    public ExecuteTransaction(Web3j web3j) {
        this.web3j = web3j;
    }

    /**
     * @param signedTransaction
     * @param callback
     * @throws IOException
     */
    public void sendTransaction(String signedTransaction, TransactionSucCallback callback)
            throws IOException {
        Request<?, SendTransaction> request = web3j.sendRawTransaction(signedTransaction);
        request.setNeedTransCallback(true);
        request.setTransactionSucCallback(callback);

        request.sendOnly();
    }

    /**
     * @param signedTransaction
     * @return
     * @throws IOException
     */
    public TransactionReceipt sendTransaction(String signedTransaction) throws IOException {

        Callback callback = new Callback();
        try {
            sendTransaction(signedTransaction, callback);

            callback.semaphore.acquire(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return callback.receipt;
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
    };
}
