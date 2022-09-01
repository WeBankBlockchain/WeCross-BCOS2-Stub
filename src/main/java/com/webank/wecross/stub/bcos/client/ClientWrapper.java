package com.webank.wecross.stub.bcos.client;

import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Wrapper interface for JavaSDK
 */
public interface ClientWrapper {
    BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException;

    BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException;

    BigInteger getBlockNumber() throws IOException;

    void sendTransaction(String signedTransactionData, TransactionCallback callback) throws IOException;

    TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash) throws IOException;

    TransactionWithProof.TransactionAndProof getTransactionByHashWithProof(String transactionHash) throws IOException;

    TransactionReceipt getTransactionReceipt(String transactionHash) throws IOException;

    JsonTransactionResponse getTransaction(String transactionHash) throws IOException;

    Call.CallOutput call(String accountAddress, String contractAddress, String data) throws IOException;
}
