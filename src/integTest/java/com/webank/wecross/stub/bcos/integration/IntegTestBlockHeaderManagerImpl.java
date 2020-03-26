package com.webank.wecross.stub.bcos.integration;

import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapper;
import java.io.IOException;
import java.math.BigInteger;

public class IntegTestBlockHeaderManagerImpl implements BlockHeaderManager {

    private Web3jWrapper web3jWrapper;

    public IntegTestBlockHeaderManagerImpl(Web3jWrapper web3jWrapper) {
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
        return new byte[0];
    }
}
