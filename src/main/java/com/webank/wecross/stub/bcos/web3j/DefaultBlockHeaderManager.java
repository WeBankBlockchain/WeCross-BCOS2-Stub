package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.bcos.BCOSConnection;
import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;

public class DefaultBlockHeaderManager implements BlockHeaderManager {
    private Web3jWrapper web3jWrapper;
    private BCOSConnection connection;

    public DefaultBlockHeaderManager(BCOSConnection connection) {
        this.connection = connection;
        this.web3jWrapper = connection.getWeb3jWrapper();
    }

    public DefaultBlockHeaderManager(Web3j web3j) {
        this.web3jWrapper = new Web3jWrapperImpl(web3j);
        this.connection = new BCOSConnection(this.web3jWrapper);
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
            return ObjectMapperFactory.getObjectMapper()
                    .writeValueAsBytes(connection.convertToBlockHeader(block));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
        try {
            BigInteger blockNumber = web3jWrapper.getBlockNumber();
            callback.onResponse(null, blockNumber.longValue());
        } catch (Exception e) {
            callback.onResponse(e, -1);
        }
    }

    @Override
    public void asyncGetBlockHeader(long blockNumber, GetBlockHeaderCallback callback) {
        final byte[] blockHeader = getBlockHeader(blockNumber);
        callback.onResponse(null, blockHeader);
    }
}
