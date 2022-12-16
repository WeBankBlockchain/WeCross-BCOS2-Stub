package com.webank.wecross.stub.bcos3.client;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.v3.client.protocol.response.Call;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

/** Wrapper interface for JavaSDK */
public interface ClientWrapper {
    BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException;

    BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException;

    BigInteger getBlockNumber() throws IOException;

    void sendTransaction(String signedTransactionData, TransactionCallback callback)
            throws IOException;

    TransactionReceipt getTransactionReceiptByHashWithProof(String transactionHash)
            throws IOException;

    JsonTransactionResponse getTransactionByHashWithProof(String transactionHash)
            throws IOException;

    TransactionReceipt getTransactionReceipt(String transactionHash) throws IOException;

    JsonTransactionResponse getTransaction(String transactionHash) throws IOException;

    Call.CallOutput call(String accountAddress, String contractAddress, byte[] data)
            throws IOException;
}
