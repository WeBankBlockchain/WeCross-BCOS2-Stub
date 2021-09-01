package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;
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
        BcosBlock bcosBlock = web3j.getBlockByNumber(BigInteger.valueOf(blockNumber), false).send();
        return bcosBlock.getResult();
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        BlockNumber blockNumber = web3j.getBlockNumber().send();
        return blockNumber.getBlockNumber();
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
        BcosTransactionReceipt bcosTransactionReceipt =
                web3j.getTransactionReceipt(transactionHash).send();
        return bcosTransactionReceipt.getResult();
    }

    @Override
    public org.fisco.bcos.web3j.protocol.core.methods.response.Transaction getTransaction(
            String transactionHash) throws IOException {
        BcosTransaction bcosTransaction = web3j.getTransactionByHash(transactionHash).send();
        return bcosTransaction.getResult();
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
