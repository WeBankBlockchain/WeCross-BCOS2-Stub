package com.webank.wecross.stub.bcos.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos.performance.PerformanceSuiteCallback;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PureBCOSProxyCallSuite extends PureBCOSProxySuite {
    private static final Logger logger = LoggerFactory.getLogger(PureBCOSProxyCallSuite.class);
    private ABIDefinition abiDefinition;
    private String methodId;

    public PureBCOSProxyCallSuite(
            String contractName, String chainName, String accountName, boolean sm)
            throws Exception {
        super(chainName, accountName, sm, contractName);

        logger.info(" ===>>> contractName : {}", contractName);
        abiDefinition = getContractABIDefinition().getFunctions().get("get").get(0);
        methodId = abiDefinition.getMethodId(getCryptoSuite());
    }

    @Override
    public String getName() {
        return "BCOS Proxy Call Suite";
    }

    @Override
    public void call(PerformanceSuiteCallback callback) {
        try {

            TransactionReceipt receipt =
                    getWeCrossProxy()
                            .constantCall(
                                    getContractName(), Numeric.hexStringToByteArray(methodId));

            if (logger.isTraceEnabled()) {
                logger.trace(" result: {} " + receipt);
            }

            if (receipt.isStatusOK()) {
                callback.onFailed("Failed! status: empty return");
            } else {
                callback.onSuccess("Success");
            }
        } catch (Exception e) {
            callback.onFailed("Failed! status: " + e.getMessage());
        }
    }
}
