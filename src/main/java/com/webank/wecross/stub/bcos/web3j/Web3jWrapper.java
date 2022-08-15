package com.webank.wecross.stub.bcos.web3j;

import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;

import java.math.BigInteger;

/** Wrapper interface for JavaSDK */
public interface Web3jWrapper {
    BcosBlock.Block getBlockByNumber(long blockNumber);

    BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber);

    BigInteger getBlockNumber();

    void sendTransaction(String signedTransactionData, TransactionCallback callback);

    TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash);

    TransactionWithProof.TransactionAndProof getTransactionByHashWithProof(String transactionHash);

    TransactionReceipt getTransactionReceipt(String transactionHash);

    JsonTransactionResponse getTransaction(String transactionHash);

    Call.CallOutput call(String accountAddress, String contractAddress, String data);
}
