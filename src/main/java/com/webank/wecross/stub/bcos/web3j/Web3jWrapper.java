package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

/** Wapper interface for JavaSDK */
public interface Web3jWrapper {
    BcosBlock getBlockByNumber(long blockNumber) throws IOException;

    BlockNumber getBlockNumber() throws IOException;

    TransactionReceipt sendTransaction(String signedTransactionData) throws IOException;

    Call call(String contractAddress, String data) throws IOException;
}
