package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.bcos.contract.BlockUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;

public class BlockManagerImplMock implements BlockManager {

    private Web3jWrapper web3jWrapper;

    public BlockManagerImplMock(Web3jWrapper web3jWrapper) {
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

    public byte[] getBlockHeader(long l) {
        try {
            BcosBlock.Block block = web3jWrapper.getBlockByNumber(l);
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
