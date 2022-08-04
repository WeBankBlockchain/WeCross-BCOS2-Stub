package com.webank.wecross.stub.bcos.verify;

import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import java.util.Objects;

import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.MerkleProofUtility;

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
            String nodeVersion)
            throws BCOSStubException {

        // verify transaction
        if (!MerkleProofUtility.verifyTransactionReceipt(
                blockHeader.getReceiptRoot(),
                transactionReceipt,
                transactionReceipt.getReceiptProof(),
                nodeVersion)) {
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
                transactionReceipt.getTxProof())) {
            throw new BCOSStubException(
                    BCOSStatusCode.TransactionProofVerifyFailed,
                    BCOSStatusCode.getStatusMessage(BCOSStatusCode.TransactionProofVerifyFailed)
                            + ", hash="
                            + hash);
        }
    }

    public interface VerifyCallback {
        void onResponse(BCOSStubException e);
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
            VerifyCallback callback) {
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
                                                + blockNumber));
                        return;
                    }

                    // verify transaction
                    if (!MerkleProofUtility.verifyTransactionReceipt(
                            block.getBlockHeader().getReceiptRoot(),
                            transactionProof.getReceiptAndProof(),
                            nodeVersion)) {
                        callback.onResponse(
                                new BCOSStubException(
                                        BCOSStatusCode.TransactionReceiptProofVerifyFailed,
                                        BCOSStatusCode.getStatusMessage(
                                                        BCOSStatusCode
                                                                .TransactionReceiptProofVerifyFailed)
                                                + ", hash="
                                                + hash));
                        return;
                    }

                    // verify transaction
                    if (!MerkleProofUtility.verifyTransaction(
                            block.getBlockHeader().getTransactionRoot(),
                            transactionProof.getTransAndProof())) {

                        callback.onResponse(
                                new BCOSStubException(
                                        BCOSStatusCode.TransactionProofVerifyFailed,
                                        BCOSStatusCode.getStatusMessage(
                                                        BCOSStatusCode.TransactionProofVerifyFailed)
                                                + ", hash="
                                                + hash));
                        return;
                    }

                    callback.onResponse(null);
                });
    }
}
