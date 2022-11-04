package com.webank.wecross.stub.bcos.common;

import java.util.List;
import org.fisco.bcos.sdk.v3.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.MerkleProofUnit;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MerkleProofUtility {
    private static final Logger logger = LoggerFactory.getLogger(MerkleProofUtility.class);

    /**
     * Verify transaction merkle proof
     *
     * @param transactionRoot
     * @param transWithProof
     * @return
     */
    public static boolean verifyTransaction(
            String transactionRoot,
            JsonTransactionResponse transWithProof,
            CryptoSuite cryptoSuite) {
        String proof =
                Merkle.calculateMerkleRoot(
                        transWithProof.getTransactionProof(),
                        transWithProof.getHash(),
                        cryptoSuite);
        logger.debug(
                " transaction hash: {}, root: {}, proof: {}",
                transWithProof.getHash(),
                transactionRoot,
                proof);

        return proof.equals(transactionRoot);
    }

    public static boolean verifyTransaction(
            String transactionRoot,
            List<MerkleProofUnit> transProof,
            String transactionHash,
            CryptoSuite cryptoSuite) {
        String proof = Merkle.calculateMerkleRoot(transProof, transactionHash, cryptoSuite);
        logger.debug(
                " transaction hash: {}, root: {}, proof: {}",
                transactionHash,
                transactionRoot,
                proof);

        return proof.equals(transactionRoot);
    }

    /**
     * Verify transaction receipt merkle proof
     *
     * @param receiptRoot
     * @param receiptWithProof
     * @return
     */
    public static boolean verifyTransactionReceipt(
            String receiptRoot, TransactionReceipt receiptWithProof, CryptoSuite cryptoSuite) {

        String proof =
                Merkle.calculateMerkleRoot(
                        receiptWithProof.getReceiptProof(),
                        receiptWithProof.getReceiptHash(),
                        cryptoSuite);

        logger.debug(
                " transaction hash: {}, root: {}, proof: {}, receipt: {}",
                receiptWithProof.getTransactionHash(),
                receiptRoot,
                proof,
                receiptWithProof);

        return proof.equals(receiptRoot);
    }
}
