package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlockHeader;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof.ReceiptAndProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof.TransAndProof;

/** Wrapper interface for JavaSDK */
public interface Web3jWrapper {
    BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException;

    BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException;

    BigInteger getBlockNumber() throws IOException;

    void sendTransaction(String signedTransactionData, TransactionSucCallback callback)
            throws IOException;

    ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash) throws IOException;

    TransAndProof getTransactionByHashWithProof(String transactionHash) throws IOException;

    TransactionReceipt getTransactionReceipt(String transactionHash) throws IOException;

    Transaction getTransaction(String transactionHash) throws IOException;

    Call.CallOutput call(String accountAddress, String contractAddress, String data)
            throws IOException;
}
