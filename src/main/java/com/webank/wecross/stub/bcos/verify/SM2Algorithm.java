package com.webank.wecross.stub.bcos.verify;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2PrivateKey;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2PublicKey;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.digests.SM3Digest;
import org.fisco.bcos.web3j.crypto.gm.sm2.util.BigIntegers;
import org.fisco.bcos.web3j.crypto.gm.sm2.util.ByteUtils;
import org.fisco.bcos.web3j.crypto.gm.sm2.util.KeyUtils;
import org.fisco.bcos.web3j.utils.Numeric;

public class SM2Algorithm {

    // SM2 recommended curves
    public static final BigInteger p =
            new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF", 16);
    public static final BigInteger a =
            new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC", 16);
    public static final BigInteger b =
            new BigInteger("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93", 16);
    public static final BigInteger n =
            new BigInteger("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123", 16);
    public static final BigInteger gx =
            new BigInteger("32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
    public static final BigInteger gy =
            new BigInteger("BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);

    public static final ECCurve sm2Curve = new ECCurve.Fp(p, a, b);
    public static final ECPoint sm2Point = sm2Curve.createPoint(gx, gy);

    /**
     * SM2 Encrypt
     *
     * @param pbk pubKey
     * @param data data to encrypt
     * @return byte[] encrypted data
     */
    public static byte[] encrypt(SM2PublicKey pbk, byte[] data) {
        String buf = KeyUtils.bcdhex_to_aschex(pbk.getEncoded());
        String pbX = buf.substring(0, 64);
        String pbY = buf.substring(64, 128);
        return encrypt(pbX, pbY, data);
    }

    /**
     * SM2 decrypt
     *
     * @param pvk secret key
     * @param cipher data to decrypt
     * @return byte[] decrypted data
     */
    public static byte[] decrypt(SM2PrivateKey pvk, byte[] cipher) {
        String buf = KeyUtils.bcdhex_to_aschex(pvk.getEncoded());
        return decrypt(buf, cipher);
    }

    /**
     * SM2 Encrypt
     *
     * @param pbkX pubKey X(hexadecimal)
     * @param pbkY pubKey Y(hexadecimal)
     * @param data data to encrypt
     * @return byte[] encrypted data
     */
    public static byte[] encrypt(String pbkX, String pbkY, byte[] data) {
        byte[] t = null;
        ECPoint c1 = null;
        BigInteger x2 = null;
        BigInteger y2 = null;
        BigInteger x1 = new BigInteger(pbkX, 16);
        BigInteger y1 = new BigInteger(pbkY, 16);
        while (isEmpty(t)) {
            BigInteger k = generateRand(32);
            c1 = calculateC1(k);
            ECPoint s = calculateS(x1, y1, k);
            x2 = calculateX2(s);
            y2 = calculateY2(s);
            if (x2.toByteArray().length >= 32 && y2.toByteArray().length >= 32) {
                t = kdf(x2, y2, data.length);
            }
        }
        byte[] c2 = calculateC2(data, t);
        byte[] c3 = calculateC3(x2, data, y2);

        byte[] c = getC(c1, c3, c2);
        return c;
    }

    /**
     * SM2 decrypt
     *
     * @param pvk secret key
     * @param data data to decrypt
     * @return byte[] decrypted data
     */
    public static byte[] decrypt(String pvk, byte[] data) {
        String hexCipher = KeyUtils.bcdhex_to_aschex(data);
        String pbX = hexCipher.substring(0, 64);
        String pbY = hexCipher.substring(64, 128);

        byte[] c3 = KeyUtils.aschex_to_bcdhex(hexCipher.substring(128, 192));
        byte[] c2 = KeyUtils.aschex_to_bcdhex(hexCipher.substring(192, hexCipher.length()));

        ECPoint s =
                calculateS(
                        new BigInteger(pbX, 16), new BigInteger(pbY, 16), new BigInteger(pvk, 16));

        ECPoint ecPoint = s.normalize();
        BigInteger x2 = ecPoint.getAffineXCoord().toBigInteger();
        BigInteger y2 = ecPoint.getAffineYCoord().toBigInteger();

        byte[] t = kdf(x2, y2, c2.length);
        if (isEmpty(t)) {
            return null;
        }

        byte[] m = calculateC2(t, c2);
        if (m == null) return null;
        byte[] cc3 = calculateC3(x2, m, y2);

        boolean sign = true;
        for (int i = 0; i < c3.length; i++) {
            if (c3.length != cc3.length) {
                sign = false;
                break;
            }
            if (c3[i] != cc3[i]) {
                sign = false;
                break;
            }
        }

        if (sign) {
            return m;
        } else {
            return null;
        }
    }

    /**
     * step 1 :random k∈[1, n-1]
     *
     * @param length random length
     * @return BigInteger random number
     */
    private static BigInteger generateRand(int length) {
        if (length > 32) {
            return null;
        }
        BigInteger k = BigInteger.ZERO;
        SecureRandom secureRandom = new SecureRandom();
        byte[] buf = new byte[length];
        while (k.compareTo(BigInteger.ZERO) <= 0 || k.compareTo(n) >= 0) {
            secureRandom.nextBytes(buf);
            k = new BigInteger(1, buf);
        }
        return k;
    }

    /** step 2: calculate the point in curve C1=[k]G=(x1,y1) */
    private static ECPoint calculateC1(BigInteger k) {
        return sm2Point.multiply(k);
    }

    /** step 3 : calculate the point in curve S=[k]Pb (Pb is pubKey) */
    private static ECPoint calculateS(BigInteger x1, BigInteger y1, BigInteger k) {
        return sm2Curve.createPoint(x1, y1).multiply(k);
    }

    /** step 4 : calculate [k]Pb=(x2,y2) */
    private static BigInteger calculateX2(ECPoint s) {
        ECPoint ecPoint = s.normalize();
        return ecPoint.getAffineXCoord().toBigInteger();
    }

    private static BigInteger calculateY2(ECPoint s) {
        ECPoint ecPoint = s.normalize();
        return ecPoint.getAffineYCoord().toBigInteger();
    }

    /** step 5 : calculate t = KDF(x2, y2, keyLen) */
    private static byte[] kdf(BigInteger x2, BigInteger y2, int keyLen) {
        byte[] t = new byte[keyLen];

        SM3Digest sm3 = new SM3Digest();
        byte[] sm3Ret = new byte[32];
        int ct = 1;

        int value = keyLen / 32;
        int remainder = keyLen % 32;

        byte[] x2Buf = padding(x2.toByteArray());
        byte[] y2Buf = padding(y2.toByteArray());

        int offset = 0;
        for (int i = 0; i < value; i++) {
            sm3.update(x2Buf, 0, x2Buf.length);
            sm3.update(y2Buf, 0, y2Buf.length);
            sm3.update((byte) (ct >> 24 & 0x00ff));
            sm3.update((byte) (ct >> 16 & 0x00ff));
            sm3.update((byte) (ct >> 8 & 0x00ff));
            sm3.update((byte) (ct & 0x00ff));
            sm3.doFinal(t, offset);
            offset += 32;
            ct++;
        }
        if (remainder != 0) {
            sm3.update(x2Buf, 0, x2Buf.length);
            sm3.update(y2Buf, 0, y2Buf.length);
            sm3.update((byte) (ct >> 24 & 0x00ff));
            sm3.update((byte) (ct >> 16 & 0x00ff));
            sm3.update((byte) (ct >> 8 & 0x00ff));
            sm3.update((byte) (ct & 0x00ff));
            sm3.doFinal(sm3Ret, 0);
        }
        System.arraycopy(sm3Ret, 0, t, offset, remainder);
        return t;
    }

    /** step 6 : calculate C2 = M xor t */
    private static byte[] calculateC2(byte[] m, byte[] t) {
        if (m == null || m.length != t.length) {
            return null;
        }
        byte[] bufOut = new byte[m.length];
        for (int i = 0; i < m.length; i++) {
            bufOut[i] = (byte) (m[i] ^ t[i]);
        }
        return bufOut;
    }

    /** step 7 : calculate C3 = Hash(X2 || M || Y2) */
    private static byte[] calculateC3(BigInteger x2, byte[] m, BigInteger y2) {
        SM3Digest sm3 = new SM3Digest();
        byte[] c3 = new byte[32];
        byte[] x2Buf = padding(x2.toByteArray());
        byte[] y2Buf = padding(y2.toByteArray());
        sm3.update(x2Buf, 0, x2Buf.length);
        sm3.update(m, 0, m.length);
        sm3.update(y2Buf, 0, y2Buf.length);
        sm3.doFinal(c3, 0);
        return c3;
    }

    /**
     * step 8 : output the cipher C = C1||C3||C2
     *
     * @param c1 pubKey part
     * @param c2 encrypt part
     * @param c3 message digest part
     */
    private static byte[] getC(ECPoint c1, byte[] c3, byte[] c2) {
        byte[] c = new byte[64 + c3.length + c2.length];

        ECPoint ecPoint = c1.normalize();
        byte[] c1xBuf = padding(ecPoint.getAffineXCoord().toBigInteger().toByteArray());
        byte[] c1yBuf = padding(ecPoint.getAffineYCoord().toBigInteger().toByteArray());

        System.arraycopy(c1xBuf, 0, c, 0, 32);
        System.arraycopy(c1yBuf, 0, c, 32, 32);
        System.arraycopy(c3, 0, c, 64, c3.length);
        System.arraycopy(c2, 0, c, 64 + c3.length, c2.length);
        return c;
    }

    private static boolean isEmpty(byte[] t) {
        if (t != null) {
            for (int i = 0; i < t.length; i++) {
                if (t[i] != (byte) 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private static byte[] padding(byte[] bi) {
        if (bi.length == 32) {
            return bi;
        } else if (bi.length > 32) {
            byte[] dest = new byte[32];
            System.arraycopy(bi, bi.length - 32, dest, 0, 32);
            return dest;
        } else {
            byte[] dest = new byte[32];
            for (int i = 0; i < 32 - bi.length; i++) {
                dest[i] = 0x00;
            }
            System.arraycopy(bi, 0, dest, 32 - bi.length, bi.length);
            return dest;
        }
    }

    public static byte[] USER_ID = KeyUtils.hex2byte("31323334353637383132333435363738");
    private static int mFieldSizeInBytes;
    public static ECCurve curve256;
    public static ECPoint g256;

    // init curve G
    static {
        curve256 = new ECCurve.Fp(p, a, b);
        g256 = curve256.createPoint(gx, gy);
        mFieldSizeInBytes = (p.bitLength() + 7 >> 3);
    }

    /**
     * SM2 sign with secKey
     *
     * @param md raw data to sign
     * @param privateKeyS secKey
     * @return Signature
     * @date 2015.12.03
     * @author fisco-bcos
     */
    private static BigInteger[] Sign(byte[] md, BigInteger privateKeyS) {
        SM3Digest sm3 = new SM3Digest();
        byte[] z = sm2GetZ(USER_ID, g256.multiply(privateKeyS));
        sm3.update(z, 0, z.length);
        byte[] p = md;
        sm3.update(p, 0, p.length);
        byte[] hashData = new byte[32];
        sm3.doFinal(hashData, 0);
        return SignSm3(hashData, privateKeyS);
    }

    /**
     * SM2 sign with secKey
     *
     * @param hash 32bytes hash
     * @param privateKeyS secKey
     * @return Signature_sm3
     * @date 2015.12.03
     * @author fisco-bcos
     */
    private static BigInteger[] SignSm3(byte[] hash, BigInteger privateKeyS) {
        byte[] hashData = ByteUtils.copyBytes(hash);
        BigInteger e = new BigInteger(1, hashData);
        BigInteger k;
        ECPoint kp;
        BigInteger r;
        BigInteger s;
        do {
            do {
                k = createRandom();
                kp = g256.multiply(k);
                ECPoint ecPoint = kp.normalize();
                r = e.add(ecPoint.getAffineXCoord().toBigInteger());
                r = r.mod(n);
            } while (r.equals(BigInteger.ZERO) || r.add(k).equals(n));
            BigInteger da_1 = privateKeyS.add(BigInteger.ONE).modInverse(n);
            s = r.multiply(privateKeyS);
            s = k.subtract(s);
            s = s.multiply(da_1);
            s = s.mod(n);
        } while (s.equals(BigInteger.ZERO));
        BigInteger[] retRS = {r, s};
        return retRS;
    }

    /**
     * SM2 verify sign with pubKey
     *
     * @param msg raw data
     * @param signData signature
     * @param biX pubKeyX
     * @param biY pubKeyY
     * @return result
     * @author fisco-bcos
     */
    private static boolean verify(byte[] msg, byte[] signData, BigInteger biX, BigInteger biY) {
        ECPoint userKey = curve256.createPoint(biX, biY);
        byte[] btRS = signData;
        byte[] btR = ByteUtils.subByteArray(btRS, 0, btRS.length / 2);
        byte[] btS = ByteUtils.subByteArray(btRS, btR.length, btRS.length - btR.length);
        BigInteger r = new BigInteger(1, btR);
        // check r ′ ∈[1, n-1]
        if (!checkValidateK(r)) return false;
        BigInteger s = new BigInteger(1, btS);
        // check s ′ ∈[1, n-1]
        if (!checkValidateK(s)) return false;

        byte[] hashData = new byte[32];
        SM3Digest sm3 = new SM3Digest();
        byte[] z = sm2GetZ(USER_ID, userKey);
        sm3.update(z, 0, z.length);
        byte[] p = msg;
        sm3.update(p, 0, p.length);
        sm3.doFinal(hashData, 0);
        BigInteger e = new BigInteger(1, hashData);

        BigInteger t = r.add(s).mod(n);
        if (t.equals(BigInteger.ZERO)) return false;
        ECPoint x1y1 = g256.multiply(s);
        x1y1 = x1y1.add(userKey.multiply(t));
        BigInteger R = e.add(x1y1.normalize().getAffineXCoord().toBigInteger()).mod(n);

        return r.equals(R);
    }

    /** * random get k ∈[1,n-1] */
    private static BigInteger createRandom() {
        SecureRandom random = new SecureRandom();
        byte[] r = new byte[32];
        BigInteger k;
        do {
            random.nextBytes(r);
            k = new BigInteger(1, r);
        } while (!checkValidateK(k));
        return k;
    }

    private static boolean checkValidateK(BigInteger k) {
        // k ∈[1,n-1]
        if (k.compareTo(new BigInteger("0")) > 0 && k.compareTo(n) < 0) {
            return true;
        }
        return false;
    }

    /**
     * calculate Za
     *
     * @param userId ID
     * @param publicKey pubKey
     * @return Z
     * @date 2015.12.04
     * @author fisco-bcos
     */
    private static byte[] sm2GetZ(byte[] userId, ECPoint publicKey) {
        SM3Digest sm3 = new SM3Digest();
        int BitsLength = userId.length << 3;
        sm3.update((byte) (BitsLength >> 8 & 0xFF));
        sm3.update((byte) (BitsLength & 0xFF));

        sm3BlockUpdate(sm3, userId);
        sm3BlockUpdate(sm3, getEncoded(a));
        sm3BlockUpdate(sm3, getEncoded(b));
        sm3BlockUpdate(sm3, getEncoded(gx));
        sm3BlockUpdate(sm3, getEncoded(gy));

        ECPoint ecPoint = publicKey.normalize();
        sm3BlockUpdate(sm3, getEncoded(ecPoint.getAffineXCoord().toBigInteger()));
        sm3BlockUpdate(sm3, getEncoded(ecPoint.getAffineYCoord().toBigInteger()));

        byte[] md = new byte[sm3.getDigestSize()];
        sm3.doFinal(md, 0);

        return md;
    }

    private static void sm3BlockUpdate(SM3Digest sm3, byte[] bytes) {
        sm3.update(bytes, 0, bytes.length);
    }

    public static byte[] getEncoded(BigInteger value) {
        byte[] bytes = BigIntegers.asUnsignedByteArray(value);
        if (bytes.length > mFieldSizeInBytes) {
            byte[] tmp = new byte[mFieldSizeInBytes];
            System.arraycopy(bytes, bytes.length - mFieldSizeInBytes, tmp, 0, mFieldSizeInBytes);
            return tmp;
        }
        if (bytes.length < mFieldSizeInBytes) {
            byte[] tmp = new byte[mFieldSizeInBytes];
            System.arraycopy(bytes, 0, tmp, mFieldSizeInBytes - bytes.length, bytes.length);
            return tmp;
        }

        return bytes;
    }

    public static byte[] sign(byte[] data, SM2PrivateKey pvk) throws IOException {
        return sign(data, pvk.getD());
    }

    public static byte[] sign(byte[] data, BigInteger privateKeyS) throws IOException {
        BigInteger[] rs = Sign(data, privateKeyS);
        byte[] r = Numeric.hexStringToByteArray(Numeric.toHexStringWithPrefixSafe(rs[0]));
        byte[] s = Numeric.hexStringToByteArray(Numeric.toHexStringWithPrefixSafe(rs[1]));
        byte[] rsb = new byte[r.length + s.length];
        System.arraycopy(r, 0, rsb, 0, r.length);
        System.arraycopy(s, 0, rsb, r.length, s.length);
        return rsb;
    }

    public static boolean verify(byte[] data, byte[] signData, String hexPbkX, String hexPbkY) {
        BigInteger biX = new BigInteger(hexPbkX, 16);
        BigInteger biY = new BigInteger(hexPbkY, 16);

        return verify(data, signData, biX, biY);
    }
}
