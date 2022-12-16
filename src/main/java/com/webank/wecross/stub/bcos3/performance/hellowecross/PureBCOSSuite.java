package com.webank.wecross.stub.bcos3.performance.hellowecross;

import com.webank.wecross.stub.bcos3.BCOS3EcdsaEvmStubFactory;
import com.webank.wecross.stub.bcos3.BCOS3GMEvmStubFactory;
import com.webank.wecross.stub.bcos3.BCOSBaseStubFactory;
import com.webank.wecross.stub.bcos3.BCOSConnection;
import com.webank.wecross.stub.bcos3.account.BCOSAccount;
import com.webank.wecross.stub.bcos3.client.AbstractClientWrapper;
import com.webank.wecross.stub.bcos3.performance.PerformanceSuite;
import java.io.File;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PureBCOSSuite implements PerformanceSuite {

    private static Logger logger = LoggerFactory.getLogger(PureBCOSSuite.class);

    private Client client;
    private CryptoKeyPair credentials;
    private CryptoSuite cryptoSuite;
    private AbstractClientWrapper abstractClientWrapper;

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

    public AbstractClientWrapper getAbstractClientWrapper() {
        return abstractClientWrapper;
    }

    public void setAbstractClientWrapper(AbstractClientWrapper abstractClientWrapper) {
        this.abstractClientWrapper = abstractClientWrapper;
    }

    public PureBCOSSuite(String chainName, String accountName, boolean sm) {

        logger.info(" chain: {}, account: {}, enableGM: {}", chainName, accountName, sm);

        BCOSBaseStubFactory stubFactory =
                (sm ? new BCOS3GMEvmStubFactory() : new BCOS3EcdsaEvmStubFactory());
        BCOSConnection connection =
                (BCOSConnection) stubFactory.newConnection("classpath:/" + chainName);

        this.abstractClientWrapper = connection.getClientWrapper();
        this.client = connection.getClientWrapper().getClient();
        this.cryptoSuite = new CryptoSuite(sm ? CryptoType.SM_TYPE : CryptoType.ECDSA_TYPE);

        BCOSAccount bcosAccount =
                (BCOSAccount)
                        stubFactory.newAccount(
                                accountName, "classpath:/accounts" + File.separator + accountName);
        this.credentials = bcosAccount.getCredentials();
    }
}
