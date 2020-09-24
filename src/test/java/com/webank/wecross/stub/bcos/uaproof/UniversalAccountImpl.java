package com.webank.wecross.stub.bcos.uaproof;

import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.UniversalAccount;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;

public class UniversalAccountImpl implements UniversalAccount {

    private Signer signer;
    private BCOSAccount bcosAccount;

    public Signer getSigner() {
        return signer;
    }

    public void setSigner(Signer signer) {
        this.signer = signer;
    }

    public UniversalAccountImpl() {
        this.bcosAccount = new BCOSAccount("UATest", "BCOS2.0", GenCredential.create());
        this.signer = Signer.newSigner(EncryptType.encryptType);
    }

    @Override
    public String getUAID() {
        return null;
    }

    @Override
    public String getPub() {
        return bcosAccount.getPub();
    }

    @Override
    public byte[] sign(byte[] message) {
        return signer.sign(bcosAccount.getCredentials().getEcKeyPair(), message);
    }

    @Override
    public boolean verify(byte[] signData, byte[] originData) {
        return signer.verify(signData, originData, bcosAccount.getPub());
    }

    @Override
    public Account getAccount(String type) {
        return bcosAccount;
    }
}
