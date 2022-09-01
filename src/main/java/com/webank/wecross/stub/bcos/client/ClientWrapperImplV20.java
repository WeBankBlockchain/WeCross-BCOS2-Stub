package com.webank.wecross.stub.bcos.client;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;

public class ClientWrapperImplV20 extends AbstractClientWrapper {

    public ClientWrapperImplV20(Client client) {
        super(client);
    }

    @Override
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }

    @Override
    public TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(
            String transactionHash) {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }

    @Override
    public TransactionWithProof.TransactionAndProof getTransactionByHashWithProof(
            String transactionHash) {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }
}
