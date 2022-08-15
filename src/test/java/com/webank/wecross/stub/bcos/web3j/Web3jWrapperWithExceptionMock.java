package com.webank.wecross.stub.bcos.web3j;

import lombok.SneakyThrows;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;
import org.fisco.bcos.sdk.client.protocol.response.Call;
import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;

import java.io.IOException;
import java.math.BigInteger;


public class Web3jWrapperWithExceptionMock extends AbstractWeb3jWrapper {

    public Web3jWrapperWithExceptionMock() {
        super(null);
    }

    @SneakyThrows
    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber){
        throw new IOException(" IOException");
    }

    @SneakyThrows
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber){
        throw new IOException(" IOException");
    }

    @SneakyThrows
    @Override
    public BigInteger getBlockNumber(){
        throw new IOException(" IOException");
    }


    @SneakyThrows
    @Override
    public void sendTransaction(String signedTransactionData, TransactionCallback callback) {
        throw new IOException(" IOException");
    }

    @SneakyThrows
    @Override
    public TransactionReceiptWithProof.ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash) {
        throw new IOException(" IOException");
    }

    @SneakyThrows
    @Override
    public TransactionWithProof.TransactionAndProof getTransactionByHashWithProof(String transactionHash) {
        throw new IOException(" IOException");
    }

    @SneakyThrows
    @Override
    public Call.CallOutput call(String accountAddress, String contractAddress, String data) {
        throw new IOException(" IOException");
    }

}
