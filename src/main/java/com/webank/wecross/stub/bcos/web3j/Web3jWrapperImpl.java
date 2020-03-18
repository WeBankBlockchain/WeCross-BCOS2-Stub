package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.contract.ExecuteTransaction;
import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.fisco.bcos.web3j.protocol.core.methods.request.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public class Web3jWrapperImpl implements Web3jWrapper {

    private Web3j web3j;
    private Service service;
    private ExecuteTransaction executeTransaction;

    public Web3jWrapperImpl(Web3j web3j, Service service) {
        this.web3j = web3j;
        this.service = service;
        this.executeTransaction = new ExecuteTransaction(web3j);
    }

    @Override
    public BcosBlock getBlockByNumber(long blockNumber) throws IOException {
        BcosBlock bcosBlock =
                web3j.getBlockByNumber(
                                DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)),
                                false)
                        .send();
        return bcosBlock;
    }

    @Override
    public BlockNumber getBlockNumber() throws IOException {
        BlockNumber blockNumber = web3j.getBlockNumber().send();
        return blockNumber;
    }

    @Override
    public TransactionReceipt sendTransaction(String signTx) throws IOException {
        return executeTransaction.sendTransaction(signTx);
    }

    @Override
    public Call call(String contractAddress, String data) throws IOException {
        Call ethCall =
                web3j.call(
                                Transaction.createEthCallTransaction("0x0", contractAddress, data),
                                DefaultBlockParameterName.LATEST)
                        .send();
        return ethCall;
    }
}
