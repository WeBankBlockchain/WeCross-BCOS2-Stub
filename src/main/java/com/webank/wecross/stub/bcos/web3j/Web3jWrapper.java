package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof.ReceiptAndProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof.TransAndProof;

/** Wapper interface for JavaSDK */
public interface Web3jWrapper {
    BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException;

    BigInteger getBlockNumber() throws IOException;

    TransactionReceipt sendTransaction(String signedTransactionData) throws IOException;

    ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash) throws IOException;

    TransAndProof getTransactionByHashWithProof(String transactionHash) throws IOException;

    Call.CallOutput call(String contractAddress, String data) throws IOException;
}
