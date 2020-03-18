package com.webank.wecross.stub.bcos.account;

import com.webank.wecross.stub.Account;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSAccount implements Account {

    private Logger logger = LoggerFactory.getLogger(BCOSAccount.class);

    private final String name;
    private final String type;
    private final String publicKey;
    private final Credentials credentials;

    public BCOSAccount(String name, String type, Credentials credentials) {
        this.name = name;
        this.type = type;
        this.credentials = credentials;
        this.publicKey =
                Numeric.toHexStringWithPrefixZeroPadded(
                        credentials.getEcKeyPair().getPublicKey(), 128);
        logger.info(" name: {}, type: {}, publicKey: {}", name, type, publicKey);
    }

    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getIdentity() {
        return publicKey;
    }
}
