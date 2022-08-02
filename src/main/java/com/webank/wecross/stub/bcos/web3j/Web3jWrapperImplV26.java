package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;

public class Web3jWrapperImplV26 extends Web3jWrapperImplV24 {

    public Web3jWrapperImplV26(Client client) {
        super(client);
    }

    @Override
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber){
        BcosBlockHeader bcosBlockHeader =
                getClient().getBlockHeaderByNumber(BigInteger.valueOf(blockNumber), true);
        return bcosBlockHeader.getBlockHeader();
    }
}
