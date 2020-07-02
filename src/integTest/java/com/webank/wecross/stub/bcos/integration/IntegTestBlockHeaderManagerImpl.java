package com.webank.wecross.stub.bcos.integration;

import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;

import java.io.IOException;
import java.math.BigInteger;

public class IntegTestBlockHeaderManagerImpl implements BlockHeaderManager {

    private Web3jWrapper web3jWrapper;
    private BCOSConnection connection;

    public IntegTestBlockHeaderManagerImpl(Web3jWrapper web3jWrapper) {
        this.web3jWrapper = web3jWrapper;
        this.connection = new BCOSConnection(web3jWrapper);
    }

    public long getBlockNumber() {
        try {
            BigInteger blockNumber = web3jWrapper.getBlockNumber();
            return blockNumber.longValue();
        } catch (IOException e) {
            return -1;
        }
    }

    public byte[] getBlockHeader(long l) {
        try {
            BcosBlock.Block block = web3jWrapper.getBlockByNumber(l);
            return ObjectMapperFactory.getObjectMapper().writeValueAsBytes(connection.convertToBlockHeader(block));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
        try {
            BigInteger blockNumber = web3jWrapper.getBlockNumber();
            callback.onResponse(null, blockNumber.longValue());
        } catch (IOException e) {
            callback.onResponse(e, -1);
        }
    }

    @Override
    public void asyncGetBlockHeader(long blockNumber, GetBlockHeaderCallback callback) {
        final byte[] blockHeader = getBlockHeader(blockNumber);
        callback.onResponse(null, blockHeader);
    }
}
