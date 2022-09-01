package com.webank.wecross.stub.bcos.uaproof;

import static junit.framework.TestCase.assertTrue;

import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.utils.Numeric;
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
                        "df9354adf11089d5107ae0ecf1ad2d379c208d76ce39109887240bf0e6c79e7fcdfda045f71a87fb3a5f42e292e5e6f71f827060fad39634c9eb0f3de7bac067"
                                + "4253897f9cd7f90d6b85cc6e12cae85e0cf7e7ed06a3f198c8178e2dd1bede737df436c94dda2c4b84cc9d2cba637f8a5ab26c63796d56b09792b7b0a1b16ab0");
        String publicKey =
                "4253897f9cd7f90d6b85cc6e12cae85e0cf7e7ed06a3f198c8178e2dd1bede737df436c94dda2c4b84cc9d2cba637f8a5ab26c63796d56b09792b7b0a1b16ab0";
        boolean verify =
                signer.verifyBySrcData(
                        sign,
                        Numeric.hexStringToByteArray(message),
                        credentials.getAddress(publicKey));
        assertTrue(verify);
    }
}
