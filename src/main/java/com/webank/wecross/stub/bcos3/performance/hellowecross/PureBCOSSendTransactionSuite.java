package com.webank.wecross.stub.bcos3.performance.hellowecross;

import com.webank.wecross.stub.bcos3.performance.PerformanceSuiteCallback;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

public class PureBCOSSendTransactionSuite extends PureBCOSSuite {
    private HelloWeCross helloWeCross;

    private List<String> ss = new ArrayList<>();

    public PureBCOSSendTransactionSuite(String chainName, String accountName, boolean sm)
            throws Exception {
        super(chainName, accountName, sm);

        helloWeCross = HelloWeCross.deploy(getClient(), getCredentials());

        ss.add("HelloWorld");
        TransactionReceipt receipt = helloWeCross.set(ss);

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
                    new TransactionCallback() {
                        @Override
                        public void onResponse(TransactionReceipt receipt) {
                            if (receipt.isStatusOK()) {
                                callback.onSuccess("Success");

                            } else {
                                callback.onFailed("Failed! status: " + receipt.getStatus());
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onFailed("Call failed: " + e);
        }
    }
}
