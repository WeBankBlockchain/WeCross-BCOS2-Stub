package com.webank.wecross.stub.bcos.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos.performance.PerformanceSuiteCallback;

public class PureBCOSProxyCallSuite extends PureBCOSProxySuite {

    public PureBCOSProxyCallSuite(
            String resourceOrAddress, String chainName, String accountName, boolean sm)
            throws Exception {
        super(chainName, accountName, sm, resourceOrAddress);
    }

    private String resourceOrAddress;

    public String getResourceOrAddress() {
        return resourceOrAddress;
    }

    public void setResourceOrAddress(String resourceOrAddress) {
        this.resourceOrAddress = resourceOrAddress;
    }

    @Override
    public String getName() {
        return "BCOS Proxy Call Suite";
    }

    @Override
    public void call(PerformanceSuiteCallback callback) {

        /*String signTx = null;
        try {
            ABIDefinition abiDefinition =
                    getContractABIDefinition().getFunctions().get("get").get(0);
            String methodId = abiDefinition.getMethodId();

            if (resourceOrAddress.startsWith("0x")) {
                signTx =
                        getWeCrossProxy()
                                .constantCall(
                                        resourceOrAddress,
                                        Numeric.hexStringToByteArray(methodId));
            } else {
                signTx =
                        getWeCrossProxy()
                                .sendTransactionSeq(
                                        resourceOrAddress,
                                        Numeric.hexStringToByteArray(methodId));
            }

            this.getWeb3j()
                    .sendRawTransactionAndGetProof(
                            signTx,
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
        */
    }
}
