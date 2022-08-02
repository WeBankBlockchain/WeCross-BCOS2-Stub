package com.webank.wecross.stub.bcos.web3j;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;

public class Web3jWrapperImplV20 extends AbstractWeb3jWrapper {

    public Web3jWrapperImplV20(Client client) {
        super(client);
    }

    @Override
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }

    @Override
    public TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash) {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }

    @Override
    public TransactionWithProof.TransactionAndProof getTransactionByHashWithProof(String transactionHash) {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }
}
