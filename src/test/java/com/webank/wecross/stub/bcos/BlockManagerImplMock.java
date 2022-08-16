package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.bcos.contract.BlockUtility;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;

import java.io.IOException;
import java.math.BigInteger;

public class BlockManagerImplMock implements BlockManager {

    private Web3jWrapper web3jWrapper;

    public BlockManagerImplMock(Web3jWrapper web3jWrapper) {
        this.web3jWrapper = web3jWrapper;
    }

    public long getBlockNumber() {
        BigInteger blockNumber = web3jWrapper.getBlockNumber();
        return blockNumber.longValue();
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
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void asyncGetBlockNumber(GetBlockNumberCallback callback) {
    }

    @Override
    public void asyncGetBlock(long blockNumber, GetBlockCallback callback) {
    }
}
