package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.request.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosTransaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosTransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public abstract class AbstractWeb3jWrapper implements Web3jWrapper {

    private Web3j web3j;
    private String version;
    private Service service;

    public AbstractWeb3jWrapper(Web3j web3j) {
        this.web3j = web3j;
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        try {
            CompletableFuture<BcosBlock> future =
                    web3j.getBlockByNumber(BigInteger.valueOf(blockNumber), false).sendAsync();

            BcosBlock bcosBlock = future.get(10, TimeUnit.SECONDS);

            return bcosBlock.getResult();
        } catch (Exception e) {
            throw new IOException("Could not getBlockByNumber: " + blockNumber);
        }
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        try {
            CompletableFuture<BlockNumber> future = web3j.getBlockNumber().sendAsync();

            BlockNumber blockNumber = future.get(10, TimeUnit.SECONDS);
            return blockNumber.getBlockNumber();
        } catch (Exception e) {
            throw new IOException("Could not getBlockNumber");
        }
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionSucCallback callback)
            throws IOException {
        web3j.sendRawTransaction(signedTransactionData, callback);
    }

    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, String data)
            throws IOException {
        Call call =
                web3j.call(
                                Transaction.createEthCallTransaction(
                                        accountAddress, contractAddress, data))
                        .send();
        return call.getResult();
    }

    @Override
    public TransactionReceipt getTransactionReceipt(String transactionHash) throws IOException {
        try {
            CompletableFuture<BcosTransactionReceipt> future =
                    web3j.getTransactionReceipt(transactionHash).sendAsync();

            BcosTransactionReceipt bcosTransactionReceipt = future.get(30, TimeUnit.SECONDS);
            return bcosTransactionReceipt.getResult();
        } catch (Exception e) {
            throw new IOException("Could not getTransactionReceipt of hash: " + transactionHash);
        }
    }

    @Override
    public org.fisco.bcos.web3j.protocol.core.methods.response.Transaction getTransaction(
            String transactionHash) throws IOException {
        try {
            CompletableFuture<BcosTransaction> future =
                    web3j.getTransactionByHash(transactionHash).sendAsync();

            BcosTransaction bcosTransaction = future.get(30, TimeUnit.SECONDS);
            return bcosTransaction.getResult();
        } catch (Exception e) {
            throw new IOException("Could not getTransaction of hash: " + transactionHash);
        }
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
