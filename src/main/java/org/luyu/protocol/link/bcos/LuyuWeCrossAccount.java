package org.luyu.protocol.link.bcos;

import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.ExtendedTransactionEncoder;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.utils.Numeric;
import org.luyu.protocol.algorithm.ecdsa.secp256k1.SignatureData;
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
        byte[] encodedExtendedRawTransaction =
                ExtendedTransactionEncoder.encode(extendedRawTransaction);
        CompletableFuture<byte[]> future = new CompletableFuture();

        luyuChainAccount.sign(
                encodedExtendedRawTransaction,
                new Account.SignCallback() {
                    @Override
                    public void onResponse(int status, String message, byte[] signBytes) {
                        if (status != Account.STATUS.OK) {
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

        try {
            byte[] luyuSignBytes = future.get(30, TimeUnit.SECONDS);
            if (getType().equals(new BCOSStubFactory().getStubType())) {
                SignatureData luyuSignData = SignatureData.parseFrom(luyuSignBytes);
                byte v = (byte) luyuSignData.getV();
                byte[] r = Numeric.toBytesPadded(luyuSignData.getR(), 32);
                byte[] s = Numeric.toBytesPadded(luyuSignData.getS(), 32);

                Sign.SignatureData signatureData = new Sign.SignatureData(v, r, s);
                byte[] signBytse =
                        ExtendedTransactionEncoder.encode(
                                extendedRawTransaction,
                                signatureData); // TODO: extendedRawTransaction encode twice, need
                // optimizing
                return signBytse;

            } else {
                // TODO: support gm
                logger.error("Unsupported account type: {}", getType());
                return null;
            }

        } catch (Exception e) {
            logger.error("LuyuChainAccount exception: ", e);
            return null;
        }
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
