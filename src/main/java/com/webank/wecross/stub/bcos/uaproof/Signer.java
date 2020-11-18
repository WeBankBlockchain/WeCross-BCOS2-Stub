package com.webank.wecross.stub.bcos.uaproof;

import com.google.common.primitives.Bytes;
import com.webank.wecross.stub.bcos.verify.SM2Algorithm;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.web3j.crypto.ECDSASign;
import org.fisco.bcos.web3j.crypto.ECDSASignature;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.SHA3Digest;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Signer {

    byte[] sign(ECKeyPair ecKeyPair, byte[] srcData);

    boolean verifyBySrcData(byte[] signData, byte[] srcData, String address);

    boolean verifyByHashData(byte[] signData, byte[] hashData, String address);

    static Signer newSigner(int encryptType) {
        return (EncryptType.SM2_TYPE == encryptType) ? new SM2Signer() : new ECDSASigner();
    }

    class ECDSASigner implements Signer {

        private static final int ECDSA_PRIVATE_KEY_SIZE = 32;

        private ECDSASign ecdsaSign = new ECDSASign();

        @Override
        public byte[] sign(ECKeyPair ecKeyPair, byte[] srcData) {
            Sign.SignatureData signatureData = ecdsaSign.secp256SignMessage(srcData, ecKeyPair);

            byte[] r = signatureData.getR();
            byte[] s = signatureData.getS();
            byte v = signatureData.getV();

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
            byte[] r = new byte[ECDSA_PRIVATE_KEY_SIZE];
            byte[] s = new byte[ECDSA_PRIVATE_KEY_SIZE];
            byte v;

            System.arraycopy(signData, 0, r, 0, r.length);
            System.arraycopy(signData, r.length, s, 0, s.length);
            v = signData[r.length + s.length];

            SHA3Digest sha3Digest = new SHA3Digest();
            byte[] hash = dataIsHash ? data : sha3Digest.hash(data);

            Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);

            ECDSASignature sig =
                    new ECDSASignature(
                            Numeric.toBigInt(signatureData.getR()),
                            Numeric.toBigInt(signatureData.getS()));

            BigInteger k = Sign.recoverFromSignature(signatureData.getV(), sig, hash);
            return Keys.getAddress(k).equals(Numeric.cleanHexPrefix(address));
        }
    }

    class SM2Signer implements Signer {

        private static final Logger logger = LoggerFactory.getLogger(SM2Signer.class);
        private static final int SM2_PUBLIC_KEY_SIZE = 32 * 2;

        @Override
        public byte[] sign(ECKeyPair ecKeyPair, byte[] srcData) {
            try {
                byte[] sign = SM2Algorithm.sign(srcData, ecKeyPair.getPrivateKey());
                byte[] pub = Numeric.toBytesPadded(ecKeyPair.getPublicKey(), SM2_PUBLIC_KEY_SIZE);
                return Bytes.concat(sign, pub);
            } catch (IOException e) {
                logger.error("e: ", e);
                throw new RuntimeException(e.getCause());
            }
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
            System.arraycopy(signData, 0, sign, 0, sign.length);
            System.arraycopy(signData, sign.length, pub, 0, pub.length);
            try {
                return SM2Algorithm.verify(
                                data,
                                sign,
                                Hex.toHexString(pub, 0, 32),
                                Hex.toHexString(pub, 32, 32))
                        && Keys.getAddress(Numeric.toHexStringNoPrefix(pub))
                                .equals(Numeric.cleanHexPrefix(address));
            } catch (Exception e) {
                logger.error("e: ", e);
                e.printStackTrace();
                throw new RuntimeException(e.getCause());
            }
        }
    }
}
