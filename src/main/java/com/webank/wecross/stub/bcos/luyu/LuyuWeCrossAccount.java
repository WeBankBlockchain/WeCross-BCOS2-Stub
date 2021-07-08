package com.webank.wecross.stub.bcos.luyu;

import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionEncoder;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2Algorithm;
import org.fisco.bcos.web3j.utils.Numeric;
import org.luyu.protocol.algorithm.ecdsa.secp256k1.SignatureData;
import org.luyu.protocol.algorithm.sm2.SM2WithSM3;
import org.luyu.protocol.common.STATUS;
import org.luyu.protocol.network.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuyuWeCrossAccount extends BCOSAccount {
    private static Logger logger = LoggerFactory.getLogger(LuyuWeCrossAccount.class);
    private org.luyu.protocol.network.Account luyuChainAccount;

    public LuyuWeCrossAccount(String type, Account luyuChainAccount) {
        super("LuyuWeCrossAccount-" + new String(luyuChainAccount.getPubKey()), type, null);
        this.luyuChainAccount = luyuChainAccount;
    }

    @Override
    public byte[] sign(ExtendedRawTransaction extendedRawTransaction) {
        try {

            if (getType().equals(BCOSConstant.BCOS_STUB_TYPE)) {
                return signNormal(extendedRawTransaction);

            } else if (getType().equals(BCOSConstant.GM_BCOS_STUB_TYPE)) {
                return signGM(extendedRawTransaction);
            } else {
                logger.error("Unsupported account type: {}", getType());
                return null;
            }

        } catch (Exception e) {
            logger.error("LuyuChainAccount exception: ", e);
            return null;
        }
    }

    public byte[] signNormal(ExtendedRawTransaction extendedRawTransaction) throws Exception {
        byte[] encodedExtendedRawTransaction =
                ExtendedTransactionEncoder.encode(extendedRawTransaction);
        CompletableFuture<byte[]> future = new CompletableFuture();

        luyuChainAccount.sign(
                encodedExtendedRawTransaction,
                new Account.SignCallback() {
                    @Override
                    public void onResponse(int status, String message, byte[] signBytes) {
                        if (status != STATUS.OK) {
                            logger.error(
                                    "LuyuChainAccount sign error, status:{} message:{}",
                                    status,
                                    message);
                            future.complete(null);
                        } else {
                            future.complete(signBytes);
                        }
                    }
                });

        byte[] luyuSignBytes = future.get(30, TimeUnit.SECONDS);

        SignatureData luyuSignData = SignatureData.parseFrom(luyuSignBytes);
        byte v = (byte) luyuSignData.getV();
        byte[] r = Numeric.toBytesPadded(luyuSignData.getR(), 32);
        byte[] s = Numeric.toBytesPadded(luyuSignData.getS(), 32);

        Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
        byte[] signBytes =
                ExtendedTransactionEncoder.encode(
                        extendedRawTransaction,
                        signatureData); // TODO: extendedRawTransaction encode twice, need
        // optimizing
        return signBytes;
    }

    public byte[] signGM(ExtendedRawTransaction extendedRawTransaction) throws Exception {
        byte[] encodedExtendedRawTransaction =
                ExtendedTransactionEncoder.encode(extendedRawTransaction);
        CompletableFuture<byte[]> future = new CompletableFuture();

        byte[] pubKey = luyuChainAccount.getPubKey();
        byte[] prepareMessage = SM2WithSM3.prepareMessage(pubKey, encodedExtendedRawTransaction);

        luyuChainAccount.sign(
                prepareMessage,
                new Account.SignCallback() {
                    @Override
                    public void onResponse(int status, String message, byte[] signBytes) {
                        if (status != STATUS.OK) {
                            logger.error(
                                    "LuyuChainAccount sign error, status:{} message:{}",
                                    status,
                                    message);
                            future.complete(null);
                        } else {
                            future.complete(signBytes);
                        }
                    }
                });

        byte[] luyuSignBytes = future.get(30, TimeUnit.SECONDS);

        SignatureData luyuSignData = SignatureData.parseFrom(luyuSignBytes);
        byte[] pub = Numeric.toBytesPadded(new BigInteger(1, pubKey), 64);
        byte[] r = SM2Algorithm.getEncoded(luyuSignData.getR());
        byte[] s = SM2Algorithm.getEncoded(luyuSignData.getS());

        Sign.SignatureData signatureData = new Sign.SignatureData((byte) 0, r, s, pub);
        byte[] signBytes =
                ExtendedTransactionEncoder.encode(
                        extendedRawTransaction,
                        signatureData); // TODO: extendedRawTransaction encode twice, need
        // optimizing
        return signBytes;
    }

    @Override
    public String getIdentity() {
        return new String(Keys.getAddress(luyuChainAccount.getPubKey()));
    }

    @Override
    public int getKeyID() {
        return -1; // not used at all
    }

    @Override
    public boolean isDefault() {
        return true; // always default
    }
}
