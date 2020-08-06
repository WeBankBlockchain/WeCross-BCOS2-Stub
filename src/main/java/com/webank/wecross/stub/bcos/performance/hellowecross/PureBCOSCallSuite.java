package com.webank.wecross.stub.bcos.performance.hellowecross;

import com.webank.wecross.stub.bcos.performance.PerformanceSuiteCallback;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PureBCOSCallSuite extends PureBCOSSuite {

    private static Logger logger = LoggerFactory.getLogger(PureBCOSCallSuite.class);

    private HelloWeCross helloWeCross;

    private List<String> ss = new ArrayList<>();

    public PureBCOSCallSuite(String chainName, String accountName, boolean sm) throws Exception {
        super(chainName, accountName, sm);
        helloWeCross =
                HelloWeCross.deploy(
                                getWeb3j(),
                                getCredentials(),
                                new BigInteger("30000000"),
                                new BigInteger("30000000"))
                        .send();

        String s = "HelloWorld" + System.currentTimeMillis();
        logger.info(
                " HelloWeCross contractAddress: {}, Message: {}",
                helloWeCross.getContractAddress(),
                s);

        ss.add(s);
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

            if ((ss.size() == stringList.size()) && (ss.get(0).equals(stringList.get(0)))) {
                callback.onSuccess("Success");
            } else {
                callback.onFailed("Result not equals: " + stringList.toString());
            }
        } catch (Exception e) {
            callback.onFailed("Call failed: " + e);
        }
    }
}
