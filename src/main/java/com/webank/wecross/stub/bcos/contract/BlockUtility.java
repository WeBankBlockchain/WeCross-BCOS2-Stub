package com.webank.wecross.stub.bcos.contract;

import com.webank.wecross.stub.BlockHeader;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;

public class BlockUtility {
    /**
     * convert Block to BlockHeader
     *
     * @param block
     * @return
     */
    public static BlockHeader convertToBlockHeader(BcosBlock.Block block) {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHash(block.getHash());
        blockHeader.setPrevHash(block.getParentHash());
        blockHeader.setNumber(block.getNumber().longValue());
        blockHeader.setReceiptRoot(block.getReceiptsRoot());
        blockHeader.setStateRoot(block.getStateRoot());
        blockHeader.setTransactionRoot(block.getTransactionsRoot());
        return blockHeader;
    }
}
