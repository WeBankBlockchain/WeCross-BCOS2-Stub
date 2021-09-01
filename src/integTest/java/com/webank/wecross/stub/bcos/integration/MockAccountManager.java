package com.webank.wecross.stub.bcos.integration;

import link.luyu.protocol.algorithm.SignAlgManager;
import link.luyu.protocol.algorithm.SignatureAlgorithm;
import link.luyu.protocol.common.STATUS;
import link.luyu.protocol.network.Account;
import link.luyu.protocol.network.LuyuSignData;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

public class MockAccountManager implements link.luyu.protocol.network.AccountManager {
    @Override
    public Account getAccountBySignature(String signatureType, byte[] LuyuSign, LuyuSignData luyuSignData) {
        SignatureAlgorithm algorithm;

        if (signatureType.equals(MockSignatureAlgorithm.TYPE)) {
            algorithm = new MockSignatureAlgorithm();
        } else {
            algorithm = SignAlgManager.getAlgorithm(signatureType);
        }
        return new MockAccount(algorithm);
    }

    @Override
    public Account getAccountByIdentity(String signatureType, String identity) {
        SignatureAlgorithm algorithm = SignAlgManager.getAlgorithm(signatureType);
        return new MockAccount(algorithm);
    }

    public static class MockAccount implements Account {
        private SignatureAlgorithm algorithm = null;


        public MockAccount(SignatureAlgorithm algorithm) {
            this.algorithm = algorithm;
        }

        @Override
        public byte[] getPubKey() {
            byte[] pubKey = new MockSignatureAlgorithm().generateKeyPair().getKey();
            return pubKey;
        }

        @Override
        public void sign(byte[] message, SignCallback callback) {
            callback.onResponse(STATUS.OK, "Success", queryAccountManagerToSign(message));
        }

        @Override
        public void verify(byte[] signBytes, byte[] message, VerifyCallback callback) {
            callback.onResponse(
                    STATUS.OK, "Success", queryAccountManagerToVerify(signBytes, message));
        }

        private byte[] queryAccountManagerToSign(byte[] message) {
            byte[] secKey = new MockSignatureAlgorithm().generateKeyPair().getValue();
            return algorithm.sign(secKey, message);
        }

        private boolean queryAccountManagerToVerify(byte[] signBytes, byte[] message) {
            return algorithm.verify(getPubKey(), signBytes, message);
        }
    }

    public static class MockSignatureAlgorithm implements SignatureAlgorithm {
        public static final String TYPE = "MOCK_SIGNATURE";

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public byte[] sign(byte[] secKey, byte[] message) {
            return (new String("sign") + String.valueOf(message)).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public boolean verify(byte[] pubKey, byte[] signBytes, byte[] message) {
            return Arrays.equals(signBytes, sign(new byte[]{}, message));
        }

        @Override
        public Map.Entry<byte[], byte[]> generateKeyPair() {
            byte[] secKey =
                    new byte[]{
                            102, -20, 99, 2, -87, 36, -103, 84, -53, -90, -24, 27, 32, -43, 116, 100, -103,
                            -36, -120, -53, 74, -38, -75, 27, -128, -24, 70, 88, 89, 61, -44, -81
                    };
            byte[] pubKey =
                    new byte[]{
                            0, -41, -66, -78, 126, -112, -32, -66, 81, -19, -64, 108, 84, 23, 40, 10, -41,
                            -128, 4, 118, -5, 1, 121, -54, 54, 88, 87, 36, 65, -114, -115, 75, -13, 123, 97,
                            62, 58, -117, 14, -67, 95, 102, 30, 56, -61, 69, -91, -39, 40, -25, -78, -111,
                            9, 0, 78, 122, -60, -10, 58, 126, 68, -70, -116, -72, -50
                    };

            return new AbstractMap.SimpleEntry<>(pubKey, secKey);
        }
    }
}
