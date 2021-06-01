package com.webank.wecross.stub.bcos.account;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

import java.io.IOException;
import java.util.Arrays;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.junit.Test;

public class BCOSAccountTest {
    @Test
    public void transactionSignTest() throws IOException {
        Credentials credentials = GenCredential.create();
        byte[] secKey = credentials.getEcKeyPair().getPrivateKey().toByteArray();
        byte[] pubKey = credentials.getEcKeyPair().getPublicKey().toByteArray();

        System.out.println(Arrays.toString(secKey));
        System.out.println(Arrays.toString(pubKey));

        BCOSAccount account = new BCOSAccount("test", "type", credentials);
        assertFalse(account.getIdentity().isEmpty());
        assertEquals(account.getName(), "test");
        assertEquals(account.getType(), "type");
    }
}
