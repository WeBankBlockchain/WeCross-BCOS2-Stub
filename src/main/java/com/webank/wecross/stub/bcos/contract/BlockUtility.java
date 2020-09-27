package com.webank.wecross.stub.bcos.contract;

import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.bcos.common.BCOSBlockHeader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlockHeader;

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

    /**
     * convert Block to BlockHeader with signature list
     *
     * @param block
     * @return
     */
    public static BlockHeader convertToBlockHeaderWithSignature(BcosBlock.Block block)
            throws IOException {
        List<String> headerData = block.getExtraData();
        BcosBlockHeader.BlockHeader bcosHeader =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(headerData.get(0), BcosBlockHeader.BlockHeader.class);

        BCOSBlockHeader stubBlockHeader = new BCOSBlockHeader();
        stubBlockHeader.setHash(bcosHeader.getHash());
        stubBlockHeader.setPrevHash(bcosHeader.getParentHash());
        stubBlockHeader.setNumber(bcosHeader.getNumber().longValue());
        stubBlockHeader.setReceiptRoot(bcosHeader.getReceiptsRoot());
        stubBlockHeader.setStateRoot(bcosHeader.getStateRoot());
        stubBlockHeader.setTransactionRoot(bcosHeader.getTransactionsRoot());
        stubBlockHeader.setSealerList(bcosHeader.getSealerList());
        stubBlockHeader.setSignatureList(bcosHeader.getSignatureList());
        return stubBlockHeader;
    }

    /**
     * @param block
     * @return
     */
    public static Block convertToBlock(BcosBlock.Block block, boolean onlyHeader)
            throws IOException {
        Block stubBlock = new Block();

        /** BlockHeader */
        BlockHeader blockHeader = convertToBlockHeaderWithSignature(block);
        stubBlock.setBlockHeader(blockHeader);

        /** tx list */
        List<String> txs = new ArrayList<>();
        if (!onlyHeader) {
            for (int i = 0; i < block.getTransactions().size(); i++) {
                BcosBlock.TransactionObject transactionObject =
                        (BcosBlock.TransactionObject) block.getTransactions().get(i);
                txs.add(transactionObject.getHash());
            }
        }
        stubBlock.setTransactionsHashes(txs);

        return stubBlock;
    }

    /**
     * @param blockBytes
     * @return
     */
    public static Block convertToBlock(byte[] blockBytes, boolean onlyHeader) throws IOException {
        BcosBlock.Block block =
                ObjectMapperFactory.getObjectMapper().readValue(blockBytes, BcosBlock.Block.class);
        Block stubBlock = convertToBlock(block, onlyHeader);
        stubBlock.setRawBytes(blockBytes);
        return stubBlock;
    }
}
