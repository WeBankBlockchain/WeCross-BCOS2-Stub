package com.webank.wecross.stub.bcos.performance.hellowecross;

import com.webank.wecross.stub.bcos.BCOSBaseStubFactory;
import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSGMStubFactory;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.performance.PerformanceSuite;
import java.io.File;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PureBCOSSuite implements PerformanceSuite {

    private static Logger logger = LoggerFactory.getLogger(PureBCOSSuite.class);

    private Client client;
    private CryptoKeyPair credentials;
    private CryptoSuite cryptoSuite;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public CryptoKeyPair getCredentials() {
        return credentials;
    }

    public void setCredentials(CryptoKeyPair credentials) {
        this.credentials = credentials;
    }

    public CryptoSuite getCryptoSuite() {
        return cryptoSuite;
    }

    public PureBCOSSuite(String chainName, String accountName, boolean sm) {

        logger.info(" chain: {}, account: {}, enableGM: {}", chainName, accountName, sm);

        BCOSBaseStubFactory stubFactory = (sm ? new BCOSGMStubFactory() : new BCOSStubFactory());
        BCOSConnection connection =
                (BCOSConnection) stubFactory.newConnection("classpath:/" + chainName);

        this.client = connection.getClientWrapper().getClient();
        this.cryptoSuite = new CryptoSuite(sm ? CryptoType.SM_TYPE : CryptoType.ECDSA_TYPE);

        BCOSAccount bcosAccount =
                (BCOSAccount)
                        stubFactory.newAccount(
                                accountName, "classpath:/accounts" + File.separator + accountName);
        this.credentials = bcosAccount.getCredentials();
    }
}
