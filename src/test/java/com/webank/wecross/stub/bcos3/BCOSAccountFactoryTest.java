package com.webank.wecross.stub.bcos3;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

import com.webank.wecross.stub.bcos3.account.BCOSAccount;
import com.webank.wecross.stub.bcos3.account.BCOSAccountFactory;
import java.io.IOException;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.junit.Test;

public class BCOSAccountFactoryTest {
    @Test
    public void loadPemTest() throws IOException {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.SM_TYPE);
        BCOSAccountFactory bcosAccountFactory = BCOSAccountFactory.getInstance(cryptoSuite);

        CryptoKeyPair cryptoKeyPair =
                bcosAccountFactory.loadPemAccount(
                        "accounts/gm_bcos/0x1bad8533b8c81962e2a07ccd4485d7a337eec8b8.pem");
        assertEquals(cryptoKeyPair.getAddress(), "0x1bad8533b8c81962e2a07ccd4485d7a337eec8b8");
        assertFalse(cryptoKeyPair.getKeyPair().getPrivate().toString().isEmpty());
    }

    @Test
    public void loadP12Test() throws IOException {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        BCOSAccountFactory bcosAccountFactory = BCOSAccountFactory.getInstance(cryptoSuite);

        CryptoKeyPair cryptoKeyPair =
                bcosAccountFactory.loadP12Account(
                        "accounts/bcos/0x4c9e341a015ce8200060a028ce45dfea8bf33e15.p12", "123456");
        assertEquals(cryptoKeyPair.getAddress(), "0x4c9e341a015ce8200060a028ce45dfea8bf33e15");
        assertFalse(cryptoKeyPair.getKeyPair().getPrivate().toString().isEmpty());
    }

    @Test
    public void buildAccountTest() throws IOException {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        BCOSAccountFactory bcosAccountFactory = BCOSAccountFactory.getInstance(cryptoSuite);

        BCOSAccount bcosAccount0 = bcosAccountFactory.build("bcos", "classpath:/accounts/bcos");
        assertEquals(
                bcosAccount0.getCredentials().getAddress(),
                "0x4c9e341a015ce8200060a028ce45dfea8bf33e15");

        assertEquals(bcosAccount0.getName(), "bcos");
        assertEquals(bcosAccount0.getType(), "BCOS3_ECDSA_EVM");
    }
}
