package com.webank.wecross.stub.bcos.performance.guomi;

import com.webank.wecross.stub.bcos.BCOSConnection;
import com.webank.wecross.stub.bcos.BCOSGMStubFactory;
import com.webank.wecross.stub.bcos.account.BCOSAccount;
import com.webank.wecross.stub.bcos.performance.PerformanceSuite;
import com.webank.wecross.stub.bcos.performance.PerformanceSuiteCallback;
import java.io.File;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public class PureBCOSCallSuite implements PerformanceSuite {
    private Web3j web3j;
    private Credentials credentials;
    private HelloWeCross helloWeCross;

    private List<String> ss = new LinkedList<>();

    public PureBCOSCallSuite(String chainName, String accountName) throws Exception {

        BCOSGMStubFactory bcosStubFactory = new BCOSGMStubFactory();

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
        return "Pure BCOS Call Suite";
    }

    @Override
    public void call(PerformanceSuiteCallback callback) {
        try {
            List<String> stringList = helloWeCross.get().send();

            if (stringList.equals(ss)) {
                callback.onSuccess("Success");
            } else {
                callback.onFailed("Result not equals: " + stringList.toString());
            }
        } catch (Exception e) {
            callback.onFailed("Call failed: " + e);
        }
    }
}
