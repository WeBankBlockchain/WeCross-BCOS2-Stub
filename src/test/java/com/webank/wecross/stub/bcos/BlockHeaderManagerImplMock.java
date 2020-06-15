package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;

public class BlockHeaderManagerImplMock implements BlockHeaderManager {

    private Web3jWrapper web3jWrapper;

    public BlockHeaderManagerImplMock(Web3jWrapper web3jWrapper) {
        this.web3jWrapper = web3jWrapper;
    }

    @Override
    public long getBlockNumber() {
        try {
            BigInteger blockNumber = web3jWrapper.getBlockNumber();
            return blockNumber.longValue();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public byte[] getBlockHeader(long l) {
        try {
            BcosBlock.Block block = web3jWrapper.getBlockByNumber(l);
            BCOSConnection connection = new BCOSConnection(null);
            BlockHeader blockHeader = connection.convertToBlockHeader(block);
            return ObjectMapperFactory.getObjectMapper().writeValueAsBytes(blockHeader);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    @Override
    public void asyncGetBlockHeader(long blockNumber, BlockHeaderCallback callback) {}
}
