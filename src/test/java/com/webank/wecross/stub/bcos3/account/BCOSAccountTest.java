package com.webank.wecross.stub.bcos3.account;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.junit.Test;

public class BCOSAccountTest {
    @Test
    public void transactionSignTest() {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        BCOSAccount account = new BCOSAccount("test", "type", cryptoSuite.generateRandomKeyPair());
        assertFalse(account.getIdentity().isEmpty());
        assertEquals(account.getName(), "test");
        assertEquals(account.getType(), "type");
    }
}
