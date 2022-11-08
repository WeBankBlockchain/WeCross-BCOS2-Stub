package com.webank.wecross.stub.bcos.client;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosTransaction;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosTransactionReceipt;
import org.fisco.bcos.sdk.v3.client.protocol.response.BlockNumber;
import org.fisco.bcos.sdk.v3.client.protocol.response.Call;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

public abstract class AbstractClientWrapper implements ClientWrapper {

    private Client client;
    private String version;
    private CryptoSuite cryptoSuite;

    public AbstractClientWrapper(Client client) {
        this.client = client;
        this.cryptoSuite =
                Objects.nonNull(client)
                        ? client.getCryptoSuite()
                        : new CryptoSuite(CryptoType.ECDSA_TYPE);
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        BcosBlock bcosBlock = client.getBlockByNumber(BigInteger.valueOf(blockNumber), false, true);
        return bcosBlock.getResult();
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        BlockNumber blockNumber = client.getBlockNumber();
        return blockNumber.getBlockNumber();
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback)
            throws IOException {
        client.sendTransactionAsync(signedTransactionData, false, callback);
    }

    @Override
    public TransactionReceipt getTransactionReceipt(String transactionHash) {
        BcosTransactionReceipt bcosTransactionReceipt =
                client.getTransactionReceipt(transactionHash, false);
        return bcosTransactionReceipt.getResult();
    }

    @Override
    public JsonTransactionResponse getTransaction(String transactionHash) {
        BcosTransaction bcosTransaction = client.getTransaction(transactionHash, false);
        return bcosTransaction.getResult();
    }

    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, byte[] data)
            throws IOException {
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
