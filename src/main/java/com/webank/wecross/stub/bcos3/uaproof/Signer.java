package com.webank.wecross.stub.bcos3.uaproof;

import com.webank.wedpr.crypto.CryptoResult;
import com.webank.wedpr.crypto.NativeInterface;
import java.security.InvalidParameterException;
import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.exceptions.SignatureException;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.crypto.signature.ECDSASignatureResult;
import org.fisco.bcos.sdk.v3.crypto.signature.SM2SignatureResult;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Signer {

    byte[] sign(CryptoKeyPair keyPair, byte[] srcData);

    boolean verifyBySrcData(byte[] signData, byte[] srcData, String address);

    boolean verifyByHashData(byte[] signData, byte[] hashData, String address);

    static Signer newSigner(int encryptType) {
        return (CryptoType.SM_TYPE == encryptType) ? new SM2Signer() : new ECDSASigner();
    }

    class ECDSASigner implements Signer {

        private static final int ECDSA_PRIVATE_KEY_SIZE = 32;
        private CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);

        @Override
        public byte[] sign(CryptoKeyPair keyPair, byte[] srcData) {
            byte[] hash = cryptoSuite.hash(srcData);
            ECDSASignatureResult sign = (ECDSASignatureResult) cryptoSuite.sign(hash, keyPair);

            byte[] r = sign.getR();
            byte[] s = sign.getS();
            byte v = sign.getV();

            byte[] result = new byte[r.length + r.length + 1];
            System.arraycopy(r, 0, result, 0, r.length);
            System.arraycopy(s, 0, result, r.length, s.length);
            result[r.length + s.length] = v;

            return result;
        }

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

            CryptoResult recoverResult =
                    NativeInterface.secp256k1RecoverPublicKey(message, signatrue);

            // call secp256k1RecoverPublicKey failed
            if (recoverResult.wedprErrorMessage != null
                    && !recoverResult.wedprErrorMessage.isEmpty()) {
                throw new SignatureException(
                        "Verify with secp256k1 failed:" + recoverResult.wedprErrorMessage);
            }

            return address.equals(
                    cryptoSuite.getKeyPairFactory().getAddress(recoverResult.publicKey));
        }
    }

    class SM2Signer implements Signer {

        private static final Logger logger = LoggerFactory.getLogger(SM2Signer.class);
        private static final int SM2_PUBLIC_KEY_SIZE = 32 * 2;
        private CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.SM_TYPE);

        @Override
        public byte[] sign(CryptoKeyPair keyPair, byte[] srcData) {
            SM2SignatureResult sign = (SM2SignatureResult) cryptoSuite.sign(srcData, keyPair);

            byte[] r = sign.getR();
            byte[] s = sign.getS();
            byte[] pub = sign.getPub();

            byte[] result = new byte[r.length + r.length + pub.length];
            System.arraycopy(r, 0, result, 0, r.length);
            System.arraycopy(s, 0, result, r.length, s.length);
            System.arraycopy(pub, 0, result, r.length + s.length, pub.length);

            return result;
        }

        @Override
        public boolean verifyBySrcData(byte[] signData, byte[] srcData, String address) {
            return verify(signData, srcData, address);
        }

        @Override
        public boolean verifyByHashData(byte[] signData, byte[] hashData, String address) {
            return verify(signData, hashData, address);
        }

        boolean verify(byte[] signData, byte[] data, String address) {
            if (signData.length < SM2_PUBLIC_KEY_SIZE) {
                throw new InvalidParameterException(
                        "the length of sign data is too short, data: " + Hex.toHexString(signData));
            }
            byte[] sign = new byte[signData.length - SM2_PUBLIC_KEY_SIZE];
            byte[] pub = new byte[SM2_PUBLIC_KEY_SIZE];
            System.arraycopy(signData, 0, sign, 0, signData.length - SM2_PUBLIC_KEY_SIZE);
            System.arraycopy(signData, sign.length, pub, 0, SM2_PUBLIC_KEY_SIZE);

            String message = Hex.toHexString(data);
            String signatrue = Hex.toHexString(sign);
            String hexPubKey = Hex.toHexString(pub);

            boolean verify = cryptoSuite.verify(hexPubKey, message, signatrue);
            String addressFromPub = cryptoSuite.getKeyPairFactory().getAddress(hexPubKey);

            return verify && addressFromPub.equals(address);
        }
    }
}
