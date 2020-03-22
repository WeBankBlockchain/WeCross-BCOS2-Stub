package com.webank.wecross.stub.bcos.web3j;

import com.webank.wecross.stub.bcos.common.BCOSConstant;
import com.webank.wecross.stub.bcos.config.BCOSStubConfig;
import com.webank.wecross.stub.bcos.contract.ExecuteTransaction;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.ChannelConnections;
import org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameterName;
import org.fisco.bcos.web3j.protocol.core.methods.request.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock;
import org.fisco.bcos.web3j.protocol.core.methods.response.BlockNumber;
import org.fisco.bcos.web3j.protocol.core.methods.response.Call;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class Web3jWrapperImpl implements Web3jWrapper {

    private static final Logger logger = LoggerFactory.getLogger(Web3jWrapperImpl.class);

    private Web3j web3j;
    private Service service;
    private ExecuteTransaction executeTransaction;

    public Web3jWrapperImpl(BCOSStubConfig.ChannelService channelServiceConfig) throws Exception {
        this.initialize(channelServiceConfig);
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public ExecuteTransaction getExecuteTransaction() {
        return executeTransaction;
    }

    public void setExecuteTransaction(ExecuteTransaction executeTransaction) {
        this.executeTransaction = executeTransaction;
    }

    public void initialize(BCOSStubConfig.ChannelService channelServiceConfig) throws Exception {

        logger.info(" ChannelService: {}", channelServiceConfig);
        logger.info(" Chain: {}", channelServiceConfig.getChain());

        EncryptType encryptType =
                new EncryptType(
                        channelServiceConfig.getChain().isEnableGM()
                                ? EncryptType.SM2_TYPE
                                : EncryptType.ECDSA_TYPE);
        logger.trace(" EncryptType: {}", encryptType.getEncryptType());

        Service service = new Service();

        /** Initialize the Web3J service */
        service.setConnectSeconds(BCOSConstant.WE3J_START_TIMEOUT_DEFAULT);
        service.setGroupId(channelServiceConfig.getChain().getGroupID());

        List<ChannelConnections> allChannelConnections = new ArrayList<>();
        ChannelConnections channelConnections = new ChannelConnections();
        channelConnections.setGroupId(channelServiceConfig.getChain().getGroupID());
        channelConnections.setConnectionsStr(channelServiceConfig.getConnectionsStr());
        allChannelConnections.add(channelConnections);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        GroupChannelConnectionsConfig groupChannelConnectionsConfig =
                new GroupChannelConnectionsConfig();

        groupChannelConnectionsConfig.setCaCert(
                resolver.getResource(channelServiceConfig.getCaCert()));
        groupChannelConnectionsConfig.setSslCert(
                resolver.getResource(channelServiceConfig.getSslCert()));
        groupChannelConnectionsConfig.setSslKey(
                resolver.getResource(channelServiceConfig.getSslKey()));
        groupChannelConnectionsConfig.setAllChannelConnections(allChannelConnections);

        service.setAllChannelConnections(groupChannelConnectionsConfig);

        // start JavaSDK service
        service.run();

        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        channelEthereumService.setTimeout(channelServiceConfig.getTimeout());
        org.fisco.bcos.web3j.protocol.Web3j web3j =
                org.fisco.bcos.web3j.protocol.Web3j.build(
                        channelEthereumService, service.getGroupId());

        this.setService(service);
        this.setWeb3j(web3j);
        this.setExecuteTransaction(new ExecuteTransaction(web3j));

        logger.info(" initialize end.");
    }

    @Override
    public BcosBlock.Block getBlockByNumber(long blockNumber) throws IOException {
        BcosBlock bcosBlock =
                web3j.getBlockByNumber(
                                DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)),
                                false)
                        .send();
        return bcosBlock.getResult();
    }

    @Override
    public BigInteger getBlockNumber() throws IOException {
        BlockNumber blockNumber = web3j.getBlockNumber().send();
        return blockNumber.getBlockNumber();
    }

    @Override
    public TransactionReceipt sendTransaction(String signTx) throws IOException {
        return executeTransaction.sendTransaction(signTx);
    }

    @Override
    public Call.CallOutput call(String contractAddress, String data) throws IOException {
        Call ethCall =
                web3j.call(
                                Transaction.createEthCallTransaction(
                                        contractAddress, contractAddress, data),
                                DefaultBlockParameterName.LATEST)
                        .send();
        return ethCall.getResult();
    }
}
