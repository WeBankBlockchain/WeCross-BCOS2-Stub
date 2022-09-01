package com.webank.wecross.stub.bcos.contract;

import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.bcos.common.BCOSBlockHeader;
import com.webank.wecross.stub.bcos.common.ObjectMapperFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlockHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockUtility {

    private static final Logger logger = LoggerFactory.getLogger(BlockUtility.class);
    /**
     * convert Block to BlockHeader
     *
     * @param block
     * @return
     */
    public static BlockHeader convertToBlockHeader(BcosBlock.Block block) throws IOException {
        List<String> headerExtraData = block.getExtraData();
        if (!headerExtraData.isEmpty() && (block.getNumber().longValue() != 0)) {
            return convertToBlockHeaderWithSignature(block);
        }
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
        List<String> headerExtraData = block.getExtraData();
        BcosBlockHeader.BlockHeader bcosHeader =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(headerExtraData.get(0), BcosBlockHeader.BlockHeader.class);

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
        BlockHeader blockHeader = convertToBlockHeader(block);
        stubBlock.setBlockHeader(blockHeader);

        /** tx list */
        List<String> txs = new ArrayList<>();
        if (!onlyHeader) {
            for (int i = 0; i < block.getTransactions().size(); i++) {
                BcosBlock.TransactionHash transactionHash =
                        (BcosBlock.TransactionHash) block.getTransactions().get(i);
                txs.add(transactionHash.get());
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
        if (logger.isDebugEnabled()) {
            logger.debug("blockNumber: {}, blockHash: {}", block.getNumber(), block.getHash());
        }
        Block stubBlock = convertToBlock(block, onlyHeader);
        stubBlock.setRawBytes(blockBytes);
        return stubBlock;
    }
}
