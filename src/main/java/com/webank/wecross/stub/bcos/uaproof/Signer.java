package com.webank.wecross.stub.bcos.uaproof;

import com.webank.wedpr.crypto.CryptoResult;
import com.webank.wedpr.crypto.NativeInterface;
import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.exceptions.SignatureException;
import org.fisco.bcos.sdk.model.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;

public interface Signer {

    boolean verifyBySrcData(byte[] signData, byte[] srcData, String address);

    boolean verifyByHashData(byte[] signData, byte[] hashData, String address);

    static Signer newSigner(int encryptType) {
        return (CryptoType.SM_TYPE == encryptType) ? new SM2Signer() : new ECDSASigner();
    }

    class ECDSASigner implements Signer {

        private static final int ECDSA_PRIVATE_KEY_SIZE = 32;
        private CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);

        @Override
        public boolean verifyBySrcData(byte[] signData, byte[] srcData, String address) {
            return verify(signData, srcData, address, false);
        }

        @Override
        public boolean verifyByHashData(byte[] signData, byte[] hashData, String address) {
            return verify(signData, hashData, address, true);
        }

        boolean verify(byte[] signData, byte[] data, String address, boolean dataIsHash) {
            byte[] hash = data;
            if (!dataIsHash) {
                hash = cryptoSuite.hash(data);
            }

            String message = Hex.toHexString(hash);
            String signatrue = Hex.toHexString(signData);

            CryptoResult recoverResult = NativeInterface.secp256k1RecoverPublicKey(message, signatrue);

            // call secp256k1RecoverPublicKey failed
            if (recoverResult.wedprErrorMessage != null && !recoverResult.wedprErrorMessage.isEmpty()) {
                throw new SignatureException(
                        "Verify with secp256k1 failed:" + recoverResult.wedprErrorMessage);
            }

            return address.equals(cryptoSuite.getKeyPairFactory().getAddress(recoverResult.publicKey));
        }
    }

    class SM2Signer implements Signer {

        private static final Logger logger = LoggerFactory.getLogger(SM2Signer.class);
        private static final int SM2_PUBLIC_KEY_SIZE = 32 * 2;
        private CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.SM_TYPE);

        @Override
        public boolean verifyBySrcData(byte[] signData, byte[] srcData, String address) {
            return verify(signData, srcData, address, false);
        }

        @Override
        public boolean verifyByHashData(byte[] signData, byte[] hashData, String address) {
            return verify(signData, hashData, address, true);
        }

        boolean verify(byte[] signData, byte[] data, String address, boolean dataIsHash) {
            if (signData.length < SM2_PUBLIC_KEY_SIZE) {
                throw new InvalidParameterException(
                        "the length of sign data is too short, data: " + Hex.toHexString(signData));
            }
            byte[] sign = new byte[signData.length - SM2_PUBLIC_KEY_SIZE];
            byte[] pub = new byte[SM2_PUBLIC_KEY_SIZE];
            System.arraycopy(signData, 0, sign, 0, sign.length);
            System.arraycopy(signData, sign.length, pub, 0, pub.length);

            byte[] hash = data;
            if (!dataIsHash) {
                hash = cryptoSuite.hash(data);
            }

            String message = Hex.toHexString(hash);
            String signatrue = Hex.toHexString(signData);

            boolean verify = cryptoSuite.verify(Hex.toHexString(pub), message, signatrue);
            String addressFromPub = cryptoSuite.getKeyPairFactory().getAddress(Hex.toHexString(pub));

            return verify && addressFromPub.equals(address);
        }
    }
}
