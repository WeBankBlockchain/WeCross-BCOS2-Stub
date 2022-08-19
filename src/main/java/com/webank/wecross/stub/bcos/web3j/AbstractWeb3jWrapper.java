package com.webank.wecross.stub.bcos.web3j;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceipt;
import org.fisco.bcos.sdk.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

public abstract class AbstractWeb3jWrapper implements Web3jWrapper {

    private Client client;
    private String version;
    private CryptoSuite cryptoSuite;

    public AbstractWeb3jWrapper(Client client) {
        this.client = client;
        this.cryptoSuite = Objects.nonNull(client) ? client.getCryptoSuite() : new CryptoSuite(CryptoType.ECDSA_TYPE);
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        BcosBlock bcosBlock = client.getBlockByNumber(BigInteger.valueOf(blockNumber), false);
        return bcosBlock.getResult();
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        BlockNumber blockNumber = client.getBlockNumber();
        return blockNumber.getBlockNumber();
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback) throws IOException {
        client.sendRawTransactionAndGetReceiptAsync(signedTransactionData, callback);
    }

    @Override
    public TransactionReceipt getTransactionReceipt(String transactionHash) {
        BcosTransactionReceipt bcosTransactionReceipt = client.getTransactionReceipt(transactionHash);
        return bcosTransactionReceipt.getResult();
    }

    @Override
    public JsonTransactionResponse getTransaction(String transactionHash) {
        BcosTransaction bcosTransaction = client.getTransactionByHash(transactionHash);
        return bcosTransaction.getResult();
    }

    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, String data) throws IOException {
        Transaction transaction = new Transaction(accountAddress, contractAddress, data);
        Call call = client.call(transaction);
        return call.getResult();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    public void setCryptoSuite(CryptoSuite cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }
}
