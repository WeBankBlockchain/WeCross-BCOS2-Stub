package com.webank.wecross.stub.bcos.account;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

import com.webank.wecross.stub.bcos.uaproof.UniversalAccountImpl;
import java.io.IOException;
import java.util.Map;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.junit.Test;

public class BCOSAccountTest {
    @Test
    public void transactionSignTest() throws IOException {
        Credentials credentials = GenCredential.create();
        BCOSAccount account = new BCOSAccount("test", "type", credentials);
        assertFalse(account.getIdentity().isEmpty());
        assertEquals(account.getName(), "test");
        assertEquals(account.getType(), "type");
    }

    @Test
    public void UAProofECDSATest() throws IOException {
        UniversalAccountImpl universalAccount = new UniversalAccountImpl();

        BCOSAccount bcosAccount = new BCOSAccount("", "", GenCredential.create());
        String proof = bcosAccount.generateUAProof(universalAccount);
        assertFalse(proof.isEmpty());
        Map.Entry<String, String> stringStringEntry =
                bcosAccount.recoverProof(proof, universalAccount);

        assertEquals(stringStringEntry.getKey(), bcosAccount.getPub());
        assertEquals(stringStringEntry.getValue(), universalAccount.getPub());
    }

    @Test
    public void UAProofSM2Test() throws IOException {
        int encryptTypeBak = EncryptType.encryptType;
        EncryptType encryptType = new EncryptType(EncryptType.SM2_TYPE);
        UniversalAccountImpl universalAccount = new UniversalAccountImpl();

        BCOSAccount bcosAccount = new BCOSAccount("", "", GenCredential.create());
        String proof = bcosAccount.generateUAProof(universalAccount);
        assertFalse(proof.isEmpty());
        Map.Entry<String, String> stringStringEntry =
                bcosAccount.recoverProof(proof, universalAccount);

        assertEquals(stringStringEntry.getKey(), bcosAccount.getPub());
        assertEquals(stringStringEntry.getValue(), universalAccount.getPub());

        EncryptType.setEncryptType(encryptTypeBak);
    }
}
