package com.webank.wecross.stub.bcos.performance.normal;

import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.performance.PerformanceSuite;
import com.webank.wecross.stub.bcos.performance.PerformanceSuiteCallback;
import java.io.File;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public class PureBCOSSendTransactionSuite implements PerformanceSuite {
    private Web3j web3j;
    private Credentials credentials;
    private HelloWeCross helloWeCross;

    private List<String> ss = new LinkedList<>();

    public PureBCOSSendTransactionSuite(String chainName, String accountName) throws Exception {

        BCOSStubFactory bcosStubFactory = new BCOSStubFactory();
        BCOSConnection connnection =
                (BCOSConnection) bcosStubFactory.newConnection("classpath:/" + chainName);

        this.web3j = connnection.getWeb3jWrapper().getWeb3j();

        BCOSAccount bcosAccount =
                (BCOSAccount)
                        bcosStubFactory.newAccount(
                                accountName, "classpath:/accounts" + File.separator + accountName);
        this.credentials = bcosAccount.getCredentials();

        helloWeCross =
                HelloWeCross.deploy(
                                web3j,
                                credentials,
                                new BigInteger("30000000"),
                                new BigInteger("30000000"))
                        .send();

        ss.add("aabbccdd");
        TransactionReceipt receipt = helloWeCross.set(ss).send();

        if (!receipt.isStatusOK()) {
            throw new Exception("Contract Init failed, status: " + receipt.getStatus());
        }
    }

    @Override
    public String getName() {
        return "Pure BCOS SendTransaction Suite";
    }

    @Override
    public void call(PerformanceSuiteCallback callback) {
        try {
            helloWeCross.set(
                    ss,
                    new TransactionSucCallback() {

                        @Override
                        public void onResponse(TransactionReceipt response) {
                            if (response.isStatusOK()) {
                                callback.onSuccess("Success");

                            } else {
                                callback.onFailed("Failed! status: " + response.getStatus());
                            }
                        }
                    });

        } catch (Exception e) {
            callback.onFailed("SendTransaction failed: " + e);
        }
    }
}
