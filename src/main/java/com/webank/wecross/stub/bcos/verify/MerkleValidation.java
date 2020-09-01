package com.webank.wecross.stub.bcos.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.BlockHeader;
import com.webank.wecross.stub.BlockHeaderManager;
import com.webank.wecross.stub.bcos.common.BCOSStatusCode;
import com.webank.wecross.stub.bcos.common.BCOSStubException;
import com.webank.wecross.stub.bcos.protocol.response.TransactionProof;
import java.util.Objects;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.MerkleProofUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MerkleValidation {
    private static final Logger logger = LoggerFactory.getLogger(MerkleValidation.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    /**
     * @param blockNumber
     * @param hash transaction hash
     * @param transactionReceipt
     * @throws BCOSStubException
     */
    public void verifyTransactionReceiptProof(
            long blockNumber,
            String hash,
            BlockHeader blockHeader,
            TransactionReceipt transactionReceipt)
            throws BCOSStubException {

        // verify transaction
        if (!MerkleProofUtility.verifyTransactionReceipt(
                blockHeader.getReceiptRoot(),
                transactionReceipt,
                transactionReceipt.getReceiptProof())) {
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
     * @param blockHeaderManager
     * @param transactionProof proof of transaction
     * @param callback
     */
    public void verifyTransactionProof(
            long blockNumber,
            String hash,
            BlockHeaderManager blockHeaderManager,
            TransactionProof transactionProof,
            VerifyCallback callback) {
        blockHeaderManager.asyncGetBlockHeader(
                blockNumber,
                (blockHeaderException, blockHeader) -> {
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
                            blockHeader.getReceiptRoot(), transactionProof.getReceiptAndProof())) {
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
                            blockHeader.getTransactionRoot(),
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
