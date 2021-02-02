package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlockHeader;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof.ReceiptAndProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof;

public class Web3jWrapperImplV20 extends AbstractWeb3jWrapper {

    public Web3jWrapperImplV20(Web3j web3j) {
        super(web3j);
    }

    @Override
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }

    @Override
    public ReceiptAndProof getTransactionReceiptByHashWithProof(String transactionHash)
            throws IOException {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }

    @Override
    public TransactionWithProof.TransAndProof getTransactionByHashWithProof(String transactionHash)
            throws IOException {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }
}
