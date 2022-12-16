package com.webank.wecross.stub.bcos3.uaproof;

import static junit.framework.TestCase.assertTrue;

import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.utils.Numeric;
import org.junit.Test;

public class SignerTest {
    @Test
    public void newECDSASignerTest() {
        Signer signer = Signer.newSigner(CryptoType.ECDSA_TYPE);
        assertTrue(signer instanceof Signer.ECDSASigner);
    }

    @Test
    public void newSM2SignerTest() {
        Signer signer = Signer.newSigner(CryptoType.SM_TYPE);
        assertTrue(signer instanceof Signer.SM2Signer);
    }

    @Test
    public void ECDSASignTest() {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        Signer signer = Signer.newSigner(CryptoType.ECDSA_TYPE);
        CryptoKeyPair credentials = cryptoSuite.getCryptoKeyPair();
        String message =
                "{\"proof\":null,\"uaPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"caPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"uaSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"caSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"timestamp\":1600866010281}";
        byte[] sign = signer.sign(credentials, message.getBytes());
        boolean verify = signer.verifyBySrcData(sign, message.getBytes(), credentials.getAddress());
        assertTrue(verify);
    }

    @Test
    public void ECDSATest() {
        Signer signer = Signer.newSigner(CryptoType.ECDSA_TYPE);
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);

        String message = "39aeeacf66784ba18836280dcb56e454fe59eecde42812503e6a0d2c0a11937f";
        byte[] sign =
                Numeric.hexStringToByteArray(
                        "b2ef97867ca3b1030c2f14be1bfeeeabddf3354ae6e57c86a02ebc1383f8ef6c7713d7f12fcfd5da457f58eeeb9aa77eddedf69b7bd1a46adb5faf072e62892601");

        String publicKey =
                "15a264e29489f69fb74608a8a26600eb8f5c572d5829531aaab3246f7411492e2c4d3c329b6f4e5fc479908a2944688da39071467035f8eb046625259a6bfd06";
        boolean verify =
                signer.verifyByHashData(
                        sign,
                        Numeric.hexStringToByteArray(message),
                        cryptoSuite.getCryptoKeyPair().getAddress(publicKey));
        assertTrue(verify);
    }

    @Test
    public void SM2SignTest() {

        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.SM_TYPE);
        Signer signer = Signer.newSigner(CryptoType.SM_TYPE);
        CryptoKeyPair credentials = cryptoSuite.getCryptoKeyPair();

        String message =
                "{\"proof\":null,\"uaPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"caPub\":\"5d614e4cc8dcb73b4326a933a8236bafc36c3219f1f22cac257b755e1d7ecff2ab5f2842b434cacee34d0a79a701b5f4c5c850c43e8f5c5e06af8dcdcfc53c8f\",\"uaSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"caSig\":\"f4df220abbf913abd6228dd31888c1a14a716a64d42186d975f6ae23f23ce279526675411e8a1cb257f358eabf4ffc99e6dfc78c36f9feceaae3de5946e24b5e01\",\"timestamp\":1600866010281}";

        byte[] sign = signer.sign(credentials, message.getBytes());
        boolean verify = signer.verifyBySrcData(sign, message.getBytes(), credentials.getAddress());

        assertTrue(verify);
    }

    @Test
    public void SM2Test() {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.SM_TYPE);
        CryptoKeyPair credentials = cryptoSuite.getCryptoKeyPair();
        Signer signer = Signer.newSigner(CryptoType.SM_TYPE);

        String message = "0a5fb1021730931b390ed3f282daf1304f86eafe4a4b74398c258e2437dcb353";
        byte[] sign =
                Numeric.hexStringToByteArray(
                        "b10e39d7b9d4318f664b18e0f3c1360342d3ce56d3e0cd72969a37fd332d522e283a89f6d36fbc942a1ed77737c399092639ad0b353aea3c14cf2c0d2a92a33d"
                                + "e3bb34b225d90ecd5b162e51ba2a962ec412454682eaf1cf2feb16e134bde5196fd6d22d66a41a45bbf1ab12c6613bfd32e40ee4ebadc6a6de73da02f3efb3f1");
        String publicKey =
                "e3bb34b225d90ecd5b162e51ba2a962ec412454682eaf1cf2feb16e134bde5196fd6d22d66a41a45bbf1ab12c6613bfd32e40ee4ebadc6a6de73da02f3efb3f1";
        boolean verify =
                signer.verifyBySrcData(
                        sign,
                        Numeric.hexStringToByteArray(message),
                        credentials.getAddress(publicKey));
        assertTrue(verify);
    }
}
