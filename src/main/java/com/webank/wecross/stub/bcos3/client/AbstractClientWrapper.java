package com.webank.wecross.stub.bcos3.client;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.v3.client.protocol.response.Call;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

public abstract class AbstractClientWrapper implements ClientWrapper {

    private Client client;
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
        BcosBlock blockByNumber =
                getClient().getBlockByNumber(BigInteger.valueOf(blockNumber), false, true);
        return blockByNumber.getBlock();
    }

    @Override
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException {
        BcosBlock blockByNumber =
                getClient().getBlockByNumber(BigInteger.valueOf(blockNumber), true, false);
        return blockByNumber.getBlock();
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        return getClient().getBlockNumber().getBlockNumber();
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback)
            throws IOException {
        getClient().sendTransactionAsync(signedTransactionData, false, callback);
    }

    @Override
    public TransactionReceipt getTransactionReceiptByHashWithProof(String transactionHash)
            throws IOException {
        return getClient().getTransactionReceipt(transactionHash, true).getResult();
    }

    @Override
    public JsonTransactionResponse getTransactionByHashWithProof(String transactionHash)
            throws IOException {
        return getClient().getTransaction(transactionHash, true).getResult();
    }

    @Override
    public TransactionReceipt getTransactionReceipt(String transactionHash) {
        return getClient().getTransactionReceipt(transactionHash, false).getResult();
    }

    @Override
    public JsonTransactionResponse getTransaction(String transactionHash) {
        return getClient().getTransaction(transactionHash, false).getResult();
    }

    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, byte[] data)
            throws IOException {
        Transaction transaction = new Transaction(accountAddress, contractAddress, data);
        return getClient().call(transaction).getCallResult();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    public void setCryptoSuite(CryptoSuite cryptoSuite) {
        this.cryptoSuite = cryptoSuite;
    }
}
