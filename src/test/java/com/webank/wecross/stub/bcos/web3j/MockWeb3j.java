package com.webank.wecross.stub.bcos.web3j;

import io.reactivex.Flowable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.Request;
import org.fisco.bcos.web3j.protocol.core.methods.request.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.*;
import org.fisco.bcos.web3j.protocol.websocket.events.LogNotification;
import org.fisco.bcos.web3j.protocol.websocket.events.NewHeadsNotification;
import org.junit.Assert;
import org.mockito.Mockito;

public class MockWeb3j implements Web3j {

    @Override
    public Request<?, NodeVersion> getNodeVersion() {

        NodeVersion nodeVersion = Mockito.mock(NodeVersion.class);
        NodeVersion.Version version = new NodeVersion.Version();
        version.setVersion("2.4.0");
        version.setSupportedVersion("2.4.0");

        Mockito.when(nodeVersion.getNodeVersion()).thenReturn(version);

        Request<?, NodeVersion> request = Mockito.mock(Request.class);
        try {
            Mockito.when(request.send()).thenReturn(nodeVersion);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        return request;
    }

    @Override
    public Request<?, BcosBlockHeader> getBlockHeaderByHash(
            String blockHash, boolean returnSealerList) {
        return null;
    }

    @Override
    public Request<?, BcosBlockHeader> getBlockHeaderByNumber(
            BigInteger blockNumber, boolean returnSealerList) {
        return null;
    }

    @Override
    public Request<?, BcosBlock> getBlockByNumber(
            BigInteger blockNumber, boolean returnFullTransactionObjects) {
        return null;
    }

    @Override
    public Request<?, BlockNumber> getBlockNumber() {
        return null;
    }

    @Override
    public Request<?, PbftView> getPbftView() {
        return null;
    }

    @Override
    public Request<?, SealerList> getSealerList() {
        return null;
    }

    @Override
    public Request<?, ObserverList> getObserverList() {
        return null;
    }

    @Override
    public Request<?, NodeIDList> getNodeIDList() {
        return null;
    }

    @Override
    public Request<?, GroupList> getGroupList() {
        return null;
    }

    @Override
    public Request<?, GroupPeers> getGroupPeers() {
        return null;
    }

    @Override
    public Request<?, Peers> getPeers() {
        return null;
    }

    @Override
    public Request<?, ConsensusStatus> getConsensusStatus() {
        return null;
    }

    @Override
    public Request<?, SyncStatus> getSyncStatus() {
        return null;
    }

    @Override
    public Request<?, SystemConfig> getSystemConfigByKey(String key) {
        return null;
    }

    @Override
    public Request<?, Code> getCode(String address, DefaultBlockParameter defaultBlockParameter) {
        return null;
    }

    @Override
    public Request<?, Code> getCode(String address) {
        return null;
    }

    @Override
    public Request<?, TotalTransactionCount> getTotalTransactionCount() {
        return null;
    }

    @Override
    public Request<?, BcosBlock> getBlockByHash(
            String blockHash, boolean returnFullTransactionObjects) {
        return null;
    }

    @Override
    public Request<?, BcosBlock> getBlockByNumber(
            DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects) {
        return null;
    }

    @Override
    public Request<?, BlockHash> getBlockHashByNumber(DefaultBlockParameter defaultBlockParameter) {
        return null;
    }

    @Override
    public Request<?, BcosTransaction> getTransactionByHash(String transactionHash) {
        return null;
    }

    @Override
    public Request<?, TransactionWithProof> getTransactionByHashWithProof(String transactionHash) {
        return null;
    }

    @Override
    public Request<?, BcosTransaction> getTransactionByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex) {
        return null;
    }

    @Override
    public Request<?, BcosTransaction> getTransactionByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger transactionIndex) {
        return null;
    }

    @Override
    public Request<?, BcosTransactionReceipt> getTransactionReceipt(String transactionHash) {
        return null;
    }

    @Override
    public Request<?, TransactionReceiptWithProof> getTransactionReceiptByHashWithProof(
            String transactionHash) {
        return null;
    }

    @Override
    public Request<?, PendingTransactions> getPendingTransaction() {
        return null;
    }

    @Override
    public BigInteger getBlockNumberCache() {
        return null;
    }

    @Override
    public Request<?, PendingTxSize> getPendingTxSize() {
        return null;
    }

    @Override
    public Request<?, Call> call(
            Transaction transaction, DefaultBlockParameter defaultBlockParameter) {
        return null;
    }

    @Override
    public Request<?, Call> call(Transaction transaction) {
        return null;
    }

    @Override
    public Request<?, SendTransaction> sendRawTransaction(String signedTransactionData) {
        return null;
    }

    @Override
    public void sendRawTransaction(String signedTransactionData, TransactionSucCallback callback)
            throws IOException {}

    @Override
    public Request<?, SendTransaction> sendRawTransactionAndGetProof(String signedTransactionData) {
        return null;
    }

    @Override
    public void sendRawTransactionAndGetProof(
            String signedTransactionData, TransactionSucCallback callback) throws IOException {}

    @Override
    public Request<?, GenerateGroup> generateGroup(
            int groupId, long timestamp, boolean enableFreeStorage, List<String> nodeList) {
        return null;
    }

    @Override
    public Request<?, StartGroup> startGroup(int groupId) {
        return null;
    }

    @Override
    public Request<?, StopGroup> stopGroup(int groupId) {
        return null;
    }

    @Override
    public Request<?, RemoveGroup> removeGroup(int groupId) {
        return null;
    }

    @Override
    public Request<?, RecoverGroup> recoverGroup(int groupId) {
        return null;
    }

    @Override
    public Request<?, QueryGroupStatus> queryGroupStatus(int groupId) {
        return null;
    }

    @Override
    public Request<?, BcosFilter> newPendingTransactionFilter() {
        return null;
    }

    @Override
    public Request<?, BcosFilter> newBlockFilter() {
        return null;
    }

    @Override
    public Request<?, BcosLog> getFilterChanges(BigInteger filterId) {
        return null;
    }

    @Override
    public Request<?, UninstallFilter> getUninstallFilter(BigInteger filterId) {
        return null;
    }

    @Override
    public Request<?, BcosFilter> newFilter(
            org.fisco.bcos.web3j.protocol.core.methods.request.BcosFilter ethFilter) {
        return null;
    }

    @Override
    public Flowable<Log> logFlowable(
            org.fisco.bcos.web3j.protocol.core.methods.request.BcosFilter filter) {
        return null;
    }

    @Override
    public Flowable<String> blockHashFlowable() {
        return null;
    }

    @Override
    public Flowable<String> pendingTransactionHashFlowable() {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            transactionFlowable() {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            pendingTransactionFlowable() {
        return null;
    }

    @Override
    public Flowable<BcosBlock> blockFlowable(boolean fullTransactionObjects) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock,
            boolean fullTransactionObjects,
            boolean ascending) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock,
            boolean fullTransactionObjects,
            Flowable<BcosBlock> onCompleteFlowable) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            replayPastTransactionsFlowable(
                    DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            replayPastTransactionsFlowable(DefaultBlockParameter startBlock) {
        return null;
    }

    @Override
    public Flowable<BcosBlock> replayPastAndFutureBlocksFlowable(
            DefaultBlockParameter startBlock, boolean fullTransactionObjects) {
        return null;
    }

    @Override
    public Flowable<org.fisco.bcos.web3j.protocol.core.methods.response.Transaction>
            replayPastAndFutureTransactionsFlowable(DefaultBlockParameter startBlock) {
        return null;
    }

    @Override
    public Flowable<NewHeadsNotification> newHeadsNotifications() {
        return null;
    }

    @Override
    public Flowable<LogNotification> logsNotifications(
            List<String> addresses, List<String> topics) {
        return null;
    }
}
