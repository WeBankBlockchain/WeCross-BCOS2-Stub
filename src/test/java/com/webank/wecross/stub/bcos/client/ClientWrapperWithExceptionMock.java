package com.webank.wecross.stub.bcos.client;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;

public class ClientWrapperWithExceptionMock extends AbstractClientWrapper {

    public ClientWrapperWithExceptionMock() {
        super(null);
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        throw new IOException(" IOException");
    }

    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback)
            throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(
            String transactionHash) throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public TransactionWithProof.TransactionAndProof getTransactionByHashWithProof(
            String transactionHash) throws IOException {
        throw new IOException(" IOException");
    }

    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, String data)
            throws IOException {
        throw new IOException(" IOException");
    }
}
