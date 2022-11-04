package com.webank.wecross.stub.bcos.client;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.request.Transaction;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.v3.client.protocol.response.Call;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

public class ClientWrapperImpl extends AbstractClientWrapper {

    public ClientWrapperImpl(Client client) {
        super(client);
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        BcosBlock blockByNumber =
                getClient().getBlockByNumber(BigInteger.valueOf(blockNumber), false, false);
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
        return getClient().getTransactionReceipt(transactionHash, true).getTransactionReceipt();
    }

    @Override
    public JsonTransactionResponse getTransactionByHashWithProof(String transactionHash)
            throws IOException {
        return getClient().getTransaction(transactionHash, true).getTransaction().get();
    }

    @Override
    public TransactionReceipt getTransactionReceipt(String transactionHash) {
        return getClient().getTransactionReceipt(transactionHash, false).getTransactionReceipt();
    }

    @Override
    public JsonTransactionResponse getTransaction(String transactionHash) {
        return getClient().getTransaction(transactionHash, true).getTransaction().get();
    }

    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, byte[] data)
            throws IOException {
        Transaction transaction = new Transaction(accountAddress, contractAddress, data);
        return getClient().call(transaction).getCallResult();
    }
}
