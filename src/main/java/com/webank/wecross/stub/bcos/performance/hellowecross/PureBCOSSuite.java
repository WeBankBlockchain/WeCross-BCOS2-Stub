package com.webank.wecross.stub.bcos.performance.hellowecross;

import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSGMStubFactory;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.performance.PerformanceSuite;
import java.io.File;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PureBCOSSuite implements PerformanceSuite {

    private static Logger logger = LoggerFactory.getLogger(PureBCOSSuite.class);

    private Web3j web3j;
    private Credentials credentials;

    public Web3j getWeb3j() {
        return web3j;
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public PureBCOSSuite(String chainName, String accountName, boolean sm) {

        logger.info(" chain: {}, account: {}, enableGM: {}", chainName, accountName, sm);

        StubFactory stubFactory = (sm ? new BCOSGMStubFactory() : new BCOSStubFactory());
        BCOSConnection connection =
                (BCOSConnection) stubFactory.newConnection("classpath:/" + chainName);

        this.web3j = connection.getWeb3jWrapper().getWeb3j();

        BCOSAccount bcosAccount =
                (BCOSAccount)
                        stubFactory.newAccount(
                                accountName, "classpath:/accounts" + File.separator + accountName);
        this.credentials = bcosAccount.getCredentials();
    }
}
