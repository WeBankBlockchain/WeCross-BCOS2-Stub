package com.webank.wecross.stub.bcos.tn;

import static org.trustnet.protocol.algorithm.ecdsa.secp256k1.Utils.toBytesPadded;

import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.common.BCOSConstant;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionEncoder;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.crypto.gm.sm2.crypto.asymmetric.SM2Algorithm;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustnet.protocol.algorithm.ecdsa.secp256k1.SignatureData;
import org.trustnet.protocol.algorithm.sm2.SM2WithSM3;
import org.trustnet.protocol.common.STATUS;
import org.trustnet.protocol.network.Account;

public class TnWeCrossAccount extends BCOSAccount {
    private static Logger logger = LoggerFactory.getLogger(TnWeCrossAccount.class);
    private Account tnChainAccount;

    public TnWeCrossAccount(String type, Account tnChainAccount) {
        super("TnWeCrossAccount-" + new String(tnChainAccount.getPubKey()), type, null);
        this.tnChainAccount = tnChainAccount;
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
            logger.error("TnChainAccount exception: ", e);
            return null;
        }
    }

    public byte[] signNormal(ExtendedRawTransaction extendedRawTransaction) throws Exception {
        byte[] encodedExtendedRawTransaction =
                ExtendedTransactionEncoder.encode(extendedRawTransaction);
        CompletableFuture<byte[]> future = new CompletableFuture();

        tnChainAccount.sign(
                encodedExtendedRawTransaction,
                new Account.SignCallback() {
                    @Override
                    public void onResponse(int status, String message, byte[] signBytes) {
                        if (status != STATUS.OK) {
                            logger.error(
                                    "TnChainAccount sign error, status:{} message:{}",
                                    status,
                                    message);
                            future.complete(null);
                        } else {
                            future.complete(signBytes);
                        }
                    }
                });

        byte[] tnSignBytes = future.get(30, TimeUnit.SECONDS);

        SignatureData tnSignData = SignatureData.parseFrom(tnSignBytes);
        byte v = (byte) tnSignData.getV();
        byte[] r = Numeric.toBytesPadded(tnSignData.getR(), 32);
        byte[] s = Numeric.toBytesPadded(tnSignData.getS(), 32);

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

        byte[] pubKey = tnChainAccount.getPubKey();
        byte[] prepareMessage = SM2WithSM3.prepareMessage(pubKey, encodedExtendedRawTransaction);

        tnChainAccount.sign(
                prepareMessage,
                new Account.SignCallback() {
                    @Override
                    public void onResponse(int status, String message, byte[] signBytes) {
                        if (status != STATUS.OK) {
                            logger.error(
                                    "TnChainAccount sign error, status:{} message:{}",
                                    status,
                                    message);
                            future.complete(null);
                        } else {
                            future.complete(signBytes);
                        }
                    }
                });

        byte[] tnSignBytes = future.get(30, TimeUnit.SECONDS);

        SignatureData tnSignData = SignatureData.parseFrom(tnSignBytes);
        byte[] pub = Numeric.toBytesPadded(new BigInteger(1, pubKey), 64);
        byte[] r = SM2Algorithm.getEncoded(tnSignData.getR());
        byte[] s = SM2Algorithm.getEncoded(tnSignData.getS());

        Sign.SignatureData signatureData = new Sign.SignatureData((byte) 0, r, s, pub);
        byte[] signBytes =
                ExtendedTransactionEncoder.encode(
                        extendedRawTransaction,
                        signatureData); // TODO: extendedRawTransaction encode twice, need
        // optimizing
        return signBytes;
    }

    private byte[] toPaddedPubKeyBytes(byte[] bytes) {
        return toBytesPadded(new BigInteger(1, bytes), 64);
    }

    @Override
    public String getIdentity() {
        String identity =
                "0x"
                        + Hex.toHexString(
                                Keys.getAddress(toPaddedPubKeyBytes(tnChainAccount.getPubKey())));
        return identity;
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
