package com.webank.wecross.stub.bcos3.account;

import com.webank.wecross.stub.Account;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCOSAccount implements Account {

    private Logger logger = LoggerFactory.getLogger(BCOSAccount.class);

    private final String name;
    private final String type;
    private final String publicKey;
    private final CryptoKeyPair keyPair;

    private int keyID;

    private boolean isDefault;

    public BCOSAccount(String name, String type, CryptoKeyPair keyPair) {
        this.name = name;
        this.type = type;
        this.keyPair = keyPair;
        this.publicKey = this.keyPair.getHexPublicKey();
        logger.info(" name: {}, type: {}, publicKey: {}", name, type, publicKey);
    }

    public CryptoKeyPair getCredentials() {
        return keyPair;
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
        return keyPair.getAddress();
    }

    @Override
    public int getKeyID() {
        return keyID;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    public void setKeyID(int keyID) {
        this.keyID = keyID;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getPub() {
        return publicKey;
    }
}
