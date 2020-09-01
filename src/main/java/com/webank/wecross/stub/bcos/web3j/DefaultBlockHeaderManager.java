package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.bcos.contract.BlockUtility;
import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;

public class DefaultBlockHeaderManager implements BlockHeaderManager {
    private Web3jWrapper web3jWrapper;

    public DefaultBlockHeaderManager(Web3jWrapper web3jWrapper) {
        this.web3jWrapper = web3jWrapper;
    }

    public long getBlockNumber() {
        try {
            BigInteger blockNumber = web3jWrapper.getBlockNumber();
            return blockNumber.longValue();
        } catch (IOException e) {
            return -1;
        }
    }

    public BlockHeader getBlockHeader(long l) throws IOException {
        BcosBlock.Block block = web3jWrapper.getBlockByNumber(l);
        return BlockUtility.convertToBlockHeader(block);
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
        try {
            BlockHeader blockHeader = getBlockHeader(blockNumber);
            callback.onResponse(null, blockHeader);
        } catch (IOException e) {
            callback.onResponse(e, null);
        }
    }
}
