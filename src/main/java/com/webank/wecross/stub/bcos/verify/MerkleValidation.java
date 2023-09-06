package com.webank.wecross.stub.bcos.verify;

import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.common.MerkleProofUtility;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import java.util.Objects;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.TransactionReceipt;

public class MerkleValidation {

    /**
     * @param hash transaction hash
     * @param transactionReceipt
     * @throws BCOSStubException
     */
    public static void verifyTransactionReceiptProof(
            String hash,
            BlockHeader blockHeader,
            TransactionReceipt transactionReceipt,
            String nodeVersion,
            CryptoSuite cryptoSuite)
            throws BCOSStubException {

        // verify transaction
        if (!MerkleProofUtility.verifyTransactionReceipt(
                blockHeader.getReceiptRoot(),
                transactionReceipt,
                transactionReceipt.getReceiptProof(),
                nodeVersion,
                cryptoSuite)) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionReceiptProofVerifyFailed,
                    BCOSStatusCode.getStatusMessage(
                                    BCOSStatusCode.TransactionReceiptProofVerifyFailed)
                            + ", hash="
                            + hash);
        }

        // verify transaction
        if (!MerkleProofUtility.verifyTransaction(
                transactionReceipt.getTransactionHash(),
                transactionReceipt.getTransactionIndex(),
                blockHeader.getTransactionRoot(),
                transactionReceipt.getTxProof(),
                cryptoSuite)) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionProofVerifyFailed,
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.TransactionProofVerifyFailed)
                            + ", hash="
                            + hash);
        }
    }

    public interface VerifyCallback {
        void onResponse(BCOSStubException e, Block block);
    }

    /**
     * @param blockNumber
     * @param hash transaction hash
     * @param blockManager
     * @param transactionProof proof of transaction
     * @param callback
     */
    public static void verifyTransactionProof(
            long blockNumber,
            String hash,
            BlockManager blockManager,
            TransactionProof transactionProof,
            String nodeVersion,
            VerifyCallback callback,
            CryptoSuite cryptoSuite) {
        blockManager.asyncGetBlock(
                blockNumber,
                (blockHeaderException, block) -> {
                    if (Objects.nonNull(blockHeaderException)) {
                        callback.onResponse(
                                new BCOSStubException(
                                        BCOSStatusCode.FetchBlockHeaderFailed,
                                        BCOSStatusCode.getStatusMessage(
                                                        BCOSStatusCode.FetchBlockHeaderFailed)
                                                + ", blockNumber: "
                                                + blockNumber),
                                null);
                        return;
                    }

                    // verify transaction
                    if (!MerkleProofUtility.verifyTransactionReceipt(
                            block.getBlockHeader().getReceiptRoot(),
                            transactionProof.getReceiptAndProof(),
                            nodeVersion,
                            cryptoSuite)) {
                        callback.onResponse(
                                new BCOSStubException(
                                        BCOSStatusCode.TransactionReceiptProofVerifyFailed,
                                        BCOSStatusCode.getStatusMessage(
                                                        BCOSStatusCode
                                                                .TransactionReceiptProofVerifyFailed)
                                                + ", hash="
                                                + hash),
                                block);
                        return;
                    }

                    // verify transaction
                    if (!MerkleProofUtility.verifyTransaction(
                            block.getBlockHeader().getTransactionRoot(),
                            transactionProof.getTransAndProof(),
                            cryptoSuite)) {

                        callback.onResponse(
                                new BCOSStubException(
                                        BCOSStatusCode.TransactionProofVerifyFailed,
                                        BCOSStatusCode.getStatusMessage(
                                                        BCOSStatusCode.TransactionProofVerifyFailed)
                                                + ", hash="
                                                + hash),
                                block);
                        return;
                    }

                    callback.onResponse(null, block);
                });
    }
}
