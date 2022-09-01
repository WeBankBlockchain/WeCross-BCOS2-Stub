package com.webank.wecross.stub.bcos.client;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;

import java.math.BigInteger;

public class ClientWrapperImplV26 extends ClientWrapperImplV24 {

    public ClientWrapperImplV26(Client client) {
        super(client);
    }

    @Override
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber){
        BcosBlockHeader bcosBlockHeader =
                getClient().getBlockHeaderByNumber(BigInteger.valueOf(blockNumber), true);
        return bcosBlockHeader.getBlockHeader();
    }
}
