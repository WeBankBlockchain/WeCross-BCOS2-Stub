package com.webank.wecross.stub.bcos3.client;

import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.bcos3.contract.BlockUtility;
import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;

public class ClientBlockManager implements BlockManager {
    private ClientWrapper clientWrapper;

    public ClientBlockManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    public long getBlockNumber() throws IOException {
        BigInteger blockNumber = clientWrapper.getBlockNumber();
        return blockNumber.longValue();
    }

    public Block getBlock(long blockNumber) throws IOException {
        BcosBlock.Block block = clientWrapper.getBlockByNumber(blockNumber);
        return BlockUtility.convertToBlock(block, false);
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
        try {
            BigInteger blockNumber = clientWrapper.getBlockNumber();
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
