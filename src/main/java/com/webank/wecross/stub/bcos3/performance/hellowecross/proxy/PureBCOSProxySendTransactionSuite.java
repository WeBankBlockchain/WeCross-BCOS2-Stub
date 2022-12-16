package com.webank.wecross.stub.bcos3.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos3.performance.PerformanceSuiteCallback;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIObject;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIObjectFactory;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PureBCOSProxySendTransactionSuite extends PureBCOSProxySuite {
    private static final Logger logger =
            LoggerFactory.getLogger(PureBCOSProxySendTransactionSuite.class);

    private List<String> ss = Arrays.asList("[\"HelloWorld" + System.currentTimeMillis() + "\"]");

    private ABIDefinition abiDefinition;
    private byte[] methodId;
    private ABIObject inputObject;
    private byte[] abi;

    public PureBCOSProxySendTransactionSuite(
            String resourceOrAddress,
            String chainName,
            String accountName,
            boolean sm,
            boolean isWasm)
            throws Exception {
        super(chainName, accountName, sm, resourceOrAddress);
        logger.info(" ===>>> resourceOrAddress : {}, value: {}", resourceOrAddress, ss.get(0));

        this.abiDefinition = getContractABIDefinition().getFunctions().get("set").get(0);
        this.methodId = abiDefinition.getMethodId(this.getCryptoSuite());
        this.inputObject = ABIObjectFactory.createInputObject(abiDefinition);
        this.abi = getAbiCodecJsonWrapper().encode(inputObject, ss).encode(isWasm);
    }

    @Override
    public String getName() {
        return "BCOS Proxy sendTransaction Suite";
    }

    @Override
    public void call(PerformanceSuiteCallback callback) {
        try {
            getWeCrossProxy()
                    .sendTransaction(
                            UUID.randomUUID().toString(),
                            getContractName(),
                            ByteUtils.merge(methodId, abi),
                            new TransactionCallback() {
                                @Override
                                public void onResponse(TransactionReceipt receipt) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(" receipt: {}", receipt);
                                    }
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
