package com.webank.wecross.stub.bcos.uaproof;

import static junit.framework.TestCase.assertTrue;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.utils.Numeric;
import org.junit.Test;

public class SignerTest {
    @Test
    public void newECDSASignerTest() {
        Signer signer = Signer.newSigner(EncryptType.ECDSA_TYPE);
        assertTrue(signer instanceof Signer.ECDSASigner);
    }

    @Test
    public void newSM2SignerTest() {
        Signer signer = Signer.newSigner(EncryptType.SM2_TYPE);
        assertTrue(signer instanceof Signer.SM2Signer);
    }

    @Test
    public void ECDSASignTest() {
        int encryptType1 = EncryptType.encryptType;
        EncryptType encryptType = new EncryptType(EncryptType.ECDSA_TYPE);
        Signer signer = Signer.newSigner(EncryptType.ECDSA_TYPE);
        Credentials credentials = GenCredential.create();

        String message =
                "{\"proof\":null,\"uaPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"caPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"uaSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"caSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"timestamp\":1600866010281}";
        byte[] sign = signer.sign(credentials.getEcKeyPair(), message.getBytes());
        String publicKey =
                Numeric.toHexStringNoPrefixZeroPadded(
                        credentials.getEcKeyPair().getPublicKey(), 128);
        boolean verify = signer.verify(sign, message.getBytes(), publicKey);
        assertTrue(verify);
        EncryptType.setEncryptType(encryptType1);
    }

    @Test
    public void SM2SignTest() {
        int encryptType1 = EncryptType.encryptType;
        EncryptType encryptType = new EncryptType(EncryptType.SM2_TYPE);
        Signer signer = Signer.newSigner(EncryptType.SM2_TYPE);
        Credentials credentials = GenCredential.create();

        String message =
                "{\"proof\":null,\"uaPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"caPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"uaSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"caSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"timestamp\":1600866010281}";
        byte[] sign = signer.sign(credentials.getEcKeyPair(), message.getBytes());
        String publicKey =
                Numeric.toHexStringNoPrefixZeroPadded(
                        credentials.getEcKeyPair().getPublicKey(), 128);
        boolean verify = signer.verify(sign, message.getBytes(), publicKey);
        assertTrue(verify);
        EncryptType.setEncryptType(encryptType1);
    }
}
