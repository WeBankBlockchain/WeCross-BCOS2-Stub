package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.bcos.contract.BlockUtility;
import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;

public class Web3jBlockManager implements BlockManager {
    private Web3jWrapper web3jWrapper;

    public Web3jBlockManager(Web3jWrapper web3jWrapper) {
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

    public Block getBlock(long l) throws IOException {
        BcosBlock.Block block = web3jWrapper.getBlockByNumber(l);
        return BlockUtility.convertToBlock(block, false);
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
    public void asyncGetBlock(long blockNumber, GetBlockCallback callback) {
        try {
            Block block = getBlock(blockNumber);
            callback.onResponse(null, block);
        } catch (IOException e) {
            callback.onResponse(e, null);
        }
    }
}
