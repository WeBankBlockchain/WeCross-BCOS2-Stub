package com.webank.wecross.stub.bcos.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos.abi.ABIDefinition;
import com.webank.wecross.stub.bcos.abi.ABIObject;
import com.webank.wecross.stub.bcos.abi.ABIObjectFactory;
import com.webank.wecross.stub.bcos.performance.PerformanceSuiteCallback;
import java.util.Arrays;
import java.util.List;
import org.fisco.bcos.channel.client.TransactionSucCallback;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PureBCOSProxySendTransactionSuite extends PureBCOSProxySuite {
    private static final Logger logger =
            LoggerFactory.getLogger(PureBCOSProxySendTransactionSuite.class);

    public PureBCOSProxySendTransactionSuite(
            String resourceOrAddress, String chainName, String accountName, boolean sm)
            throws Exception {
        super(chainName, accountName, sm, resourceOrAddress);
        logger.info(" ===>>> resourceOrAddress : {}, value: {}", resourceOrAddress, ss.get(0));
    }

    private List<String> ss = Arrays.asList("[\"HelloWorld" + System.currentTimeMillis() + "\"]");

    private ABIDefinition abiDefinition =
            getContractABIDefinition().getFunctions().get("set").get(0);
    private String methodId = abiDefinition.getMethodId();
    private ABIObject inputObject = ABIObjectFactory.createInputObject(abiDefinition);
    private String abi = getAbiCodecJsonWrapper().encode(inputObject, ss).encode();

    @Override
    public String getName() {
        return "BCOS Proxy sendTransaction Suite";
    }

    @Override
    public void call(PerformanceSuiteCallback callback) {
        String signTx = null;
        try {
            if (getResource().isEmpty()) {
                signTx =
                        getWeCrossProxy()
                                .sendTransactionByAddressSeq(
                                        getAddress(), Numeric.hexStringToByteArray(methodId + abi));
            } else {
                signTx =
                        getWeCrossProxy()
                                .sendTransactionSeq(
                                        getResource(),
                                        Numeric.hexStringToByteArray(methodId + abi));
            }

            this.getWeb3j()
                    .sendRawTransactionAndGetProof(
                            signTx,
                            new TransactionSucCallback() {
                                @Override
                                public void onResponse(TransactionReceipt response) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(" receipt: {}", response);
                                    }
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
