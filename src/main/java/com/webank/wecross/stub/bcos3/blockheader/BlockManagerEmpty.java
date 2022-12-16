package com.webank.wecross.stub.bcos3.blockheader;

import com.webank.wecross.stub.BlockManager;

public class BlockManagerEmpty implements BlockManager {
    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
        callback.onResponse(null, 0);
    }

    @Override
    public void asyncGetBlock(long blockNumber, GetBlockCallback callback) {
        callback.onResponse(null, null);
    }
}
