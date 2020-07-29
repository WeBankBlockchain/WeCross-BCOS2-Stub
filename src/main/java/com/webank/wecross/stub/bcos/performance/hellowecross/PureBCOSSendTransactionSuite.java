package com.webank.wecross.stub.bcos.performance.hellowecross;

import com.webank.wecross.stub.bcos.performance.PerformanceSuiteCallback;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;

public class PureBCOSSendTransactionSuite extends PureBCOSSuite {
    private HelloWeCross helloWeCross;

    private List<String> ss = new ArrayList<>();

    public PureBCOSSendTransactionSuite(String chainName, String accountName, boolean sm)
            throws Exception {
        super(chainName, accountName, sm);

        helloWeCross =
                HelloWeCross.deploy(
                                getWeb3j(),
                                getCredentials(),
                                new BigInteger("30000000"),
                                new BigInteger("30000000"))
                        .send();

        ss.add("HelloWorld");
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
            this.getWeb3j()
                    .sendRawTransactionAndGetProof(
                            helloWeCross.setSeq(ss),
                            new TransactionSucCallback() {

                                @Override
                                public void onResponse(TransactionReceipt response) {
                                    if (response.isStatusOK()) {
                                        callback.onSuccess("Success");

                                    } else {
                                        callback.onFailed(
                                                "Failed! status: " + response.getStatus());
                                    }
                                }
                            });
        } catch (Exception e) {
            callback.onFailed("Call failed: " + e);
        }
    }
}
