package com.webank.wecross.stub.bcos3;

import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.bcos3.client.ClientWrapper;
import com.webank.wecross.stub.bcos3.common.ObjectMapperFactory;
import com.webank.wecross.stub.bcos3.contract.BlockUtility;
import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.sdk.v3.client.protocol.response.BcosBlock;

public class BlockManagerImplMock implements BlockManager {

    private ClientWrapper clientWrapper;

    public BlockManagerImplMock(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    public long getBlockNumber() throws IOException {
        BigInteger blockNumber = clientWrapper.getBlockNumber();
        return blockNumber.longValue();
    }

    public byte[] getBlockHeader(long l) {
        try {
            BcosBlock.Block block = clientWrapper.getBlockByNumber(l);
            BlockHeader blockHeader = BlockUtility.convertToBlockHeader(block);
            return ObjectMapperFactory.getObjectMapper().writeValueAsBytes(blockHeader);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void asyncGetBlockNumber(GetBlockNumberCallback callback) {}

    @Override
    public void asyncGetBlock(long blockNumber, GetBlockCallback callback) {}
}
