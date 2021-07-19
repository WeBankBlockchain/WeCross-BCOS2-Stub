package com.webank.wecross.stub.bcos.web3j;

import java.io.IOException;
import java.math.BigInteger;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlockHeader;

public class Web3jWrapperImplV26 extends Web3jWrapperImplV24 {

    public Web3jWrapperImplV26(Web3j web3j) {
        super(web3j);
    }

    @Override
    public BcosBlockHeader.BlockHeader getBlockHeaderByNumber(long blockNumber) throws IOException {
        BcosBlockHeader bcosBlockHeader =
                getWeb3j().getBlockHeaderByNumber(BigInteger.valueOf(blockNumber), true).send();
        return bcosBlockHeader.getBlockHeader();
    }
}
