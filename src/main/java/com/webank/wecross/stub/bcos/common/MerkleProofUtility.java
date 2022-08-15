package com.webank.wecross.stub.bcos.common;


import org.fisco.bcos.sdk.channel.model.ChannelPrococolExceiption;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.TransactionReceiptWithProof;
import org.fisco.bcos.sdk.client.protocol.response.TransactionWithProof;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.MerkleProofUnit;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.rlp.RlpEncoder;
import org.fisco.bcos.sdk.rlp.RlpString;
import org.fisco.bcos.sdk.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @projectName: bcos2-stub
 * @package: com.webank.wecross.stub.bcos.common
 * @className: MerkleProofUtility
 * @date: 2022/8/2 16:03
 */
public class MerkleProofUtility {
    private static final Logger logger = LoggerFactory.getLogger(MerkleProofUtility.class);

    /**
     * Verify transaction merkle proof
     *
     * @param transactionRoot
     * @param transAndProof
     * @return
     */
    public static boolean verifyTransaction(
            String transactionRoot, TransactionWithProof.TransactionAndProof transAndProof, CryptoSuite cryptoSuite) {
        // transaction index
        JsonTransactionResponse transaction = transAndProof.getTransaction();
        String index = transaction.getTransactionIndex();
        String input =
                Numeric.toHexString(RlpEncoder.encode(RlpString.create(index)))
                        + transaction.getHash().substring(2);
        String proof = Merkle.calculateMerkleRoot(transAndProof.getTransactionProof(), input, cryptoSuite);

        logger.debug(
                " transaction hash: {}, transaction index: {}, root: {}, proof: {}",
                transaction.getHash(),
                transaction.getTransactionIndex(),
                transactionRoot,
                proof);

        return proof.equals(transactionRoot);
    }

    /**
     * Verify transaction receipt merkle proof
     *
     * @param receiptRoot
     * @param receiptAndProof
     * @return
     */
    public static boolean verifyTransactionReceipt(
            String receiptRoot,
            TransactionReceiptWithProof.ReceiptAndProof receiptAndProof,
            String supportedVersion, CryptoSuite cryptoSuite) {

        EnumNodeVersion.Version classVersion = null;
        try {
            classVersion = EnumNodeVersion.getClassVersion(supportedVersion);
        } catch (ChannelPrococolExceiption e) {
        }

        TransactionReceipt transactionReceipt = receiptAndProof.getReceipt();

        // transaction index
        byte[] byteIndex =
                RlpEncoder.encode(RlpString.create(transactionReceipt.getTransactionIndex()));

        if (!transactionReceipt.getGasUsed().startsWith("0x")) {
            transactionReceipt.setGasUsed("0x" + transactionReceipt.getGasUsed());
        }

        if (classVersion != null && classVersion.getMinor() >= 9) {
            if (!transactionReceipt.getRemainGas().startsWith("0x")) {
                transactionReceipt.setRemainGas(
                        "0x" + transactionReceipt.getRemainGas());
            }
        }

        String receiptRlp = ReceiptEncoder.encode(transactionReceipt, classVersion);
        String rlpHash = cryptoSuite.hash(receiptRlp);
        String input = Numeric.toHexString(byteIndex) + rlpHash.substring(2);

        String proof = Merkle.calculateMerkleRoot(receiptAndProof.getReceiptProof(), input, cryptoSuite);

        logger.debug(
                " transaction hash: {}, receipt index: {}, root: {}, proof: {}, receipt: {}",
                transactionReceipt.getTransactionHash(),
                transactionReceipt.getTransactionIndex(),
                receiptRoot,
                proof,
                receiptAndProof.getReceipt());

        return proof.equals(receiptRoot);
    }

    /**
     * Verify transaction merkle proof
     *
     * @param transactionHash
     * @param index
     * @param transactionRoot
     * @param txProof
     * @return
     */
    public static boolean verifyTransaction(
            String transactionHash,
            String index,
            String transactionRoot,
            List<MerkleProofUnit> txProof, CryptoSuite cryptoSuite) {
        String input =
                Numeric.toHexString(RlpEncoder.encode(RlpString.create(index)))
                        + transactionHash.substring(2);
        String proof = Merkle.calculateMerkleRoot(txProof, input, cryptoSuite);

        logger.debug(
                " transaction hash: {}, transaction index: {}, txProof: {}, transactionRoot: {}, proof: {}",
                transactionHash,
                index,
                txProof,
                transactionRoot,
                proof);

        return proof.equals(transactionRoot);
    }

    /**
     * Verify transaction receipt merkle proof
     *
     * @param receiptRoot
     * @param transactionReceipt
     * @param receiptProof
     * @return
     */
    public static boolean verifyTransactionReceipt(
            String receiptRoot,
            TransactionReceipt transactionReceipt,
            List<MerkleProofUnit> receiptProof,
            String supportedVersion, CryptoSuite cryptoSuite) {

        if (!transactionReceipt.getGasUsed().startsWith("0x")) {
            transactionReceipt.setGasUsed("0x" + transactionReceipt.getGasUsed());
        }

        EnumNodeVersion.Version classVersion = null;
        try {
            classVersion = EnumNodeVersion.getClassVersion(supportedVersion);
        } catch (ChannelPrococolExceiption e) {
        }

        if (classVersion != null && classVersion.getMinor() >= 9) {
            if (!transactionReceipt.getRemainGas().startsWith("0x")) {
                transactionReceipt.setRemainGas(
                        "0x" + transactionReceipt.getRemainGas());
            }
        }

        // transaction index
        byte[] byteIndex =
                RlpEncoder.encode(RlpString.create(transactionReceipt.getTransactionIndex()));

        String receiptRlp = ReceiptEncoder.encode(transactionReceipt, classVersion);
        //String rlpHash = Hash.sha3(receiptRlp);
        String rlpHash = cryptoSuite.hash(receiptRlp);
        String input = Numeric.toHexString(byteIndex) + rlpHash.substring(2);

        String proof = Merkle.calculateMerkleRoot(receiptProof, input, cryptoSuite);

        logger.debug(
                " transaction hash: {}, transactionReceipt: {}, receiptProof: {}, receiptRoot: {}, proof: {}",
                transactionReceipt.getTransactionHash(),
                transactionReceipt,
                receiptProof,
                receiptRoot,
                proof);

        return proof.equals(receiptRoot);
    }

}
