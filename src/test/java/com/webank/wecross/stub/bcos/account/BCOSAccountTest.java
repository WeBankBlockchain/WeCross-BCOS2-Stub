package com.webank.wecross.stub.bcos.account;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.CryptoType;
import org.junit.Test;

public class BCOSAccountTest {
    @Test
    public void transactionSignTest() {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        BCOSAccount account = new BCOSAccount("test", "type", cryptoSuite.createKeyPair());
        assertFalse(account.getIdentity().isEmpty());
        assertEquals(account.getName(), "test");
        assertEquals(account.getType(), "type");
    }
}
