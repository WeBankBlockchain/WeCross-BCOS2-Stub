package com.webank.wecross.stub.bcos;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.account.BCOSAccountFactory;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.junit.Test;

public class BCSOAccountFactoryTest {
    @Test
    public void loadPemTest()
            throws IOException, CertificateException, UnrecoverableKeyException,
                    NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
                    InvalidKeySpecException {
        new EncryptType(EncryptType.SM2_TYPE);
        Credentials credentials =
                BCOSAccountFactory.loadPemAccount(
                        "accounts/gm_bcos/0x1bad8533b8c81962e2a07ccd4485d7a337eec8b8.pem");
        assertEquals(credentials.getAddress(), "0x1bad8533b8c81962e2a07ccd4485d7a337eec8b8");
        assertFalse(credentials.getEcKeyPair().getPrivateKey().toString().isEmpty());
        new EncryptType(EncryptType.ECDSA_TYPE);
    }

    @Test
    public void loadP12Test()
            throws IOException, CertificateException, UnrecoverableKeyException,
                    NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
                    InvalidKeySpecException {
        Credentials credentials =
                BCOSAccountFactory.loadP12Account(
                        "accounts/bcos/0x4c9e341a015ce8200060a028ce45dfea8bf33e15.p12", "123456");
        assertEquals(credentials.getAddress(), "0x4c9e341a015ce8200060a028ce45dfea8bf33e15");
        assertTrue(!credentials.getEcKeyPair().getPrivateKey().toString().isEmpty());
    }

    @Test
    public void buildAccountTest()
            throws IOException, CertificateException, UnrecoverableKeyException,
                    NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException,
                    InvalidKeySpecException {

        BCOSAccount bcosAccount0 = BCOSAccountFactory.build("bcos", "classpath:/accounts/bcos");
        assertEquals(
                bcosAccount0.getCredentials().getAddress(),
                "0x4c9e341a015ce8200060a028ce45dfea8bf33e15");

        assertEquals(bcosAccount0.getName(), "bcos");

        assertEquals(bcosAccount0.getType(), "BCOS2.0");
    }
}
