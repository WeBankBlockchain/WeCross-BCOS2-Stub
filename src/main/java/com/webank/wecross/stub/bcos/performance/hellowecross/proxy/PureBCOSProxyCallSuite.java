package com.webank.wecross.stub.bcos.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos.performance.PerformanceSuiteCallback;
import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.utils.Numeric;
import org.fisco.bcos.web3j.abi.wrapper.ABIDefinition;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PureBCOSProxyCallSuite extends PureBCOSProxySuite {
    private static final Logger logger = LoggerFactory.getLogger(PureBCOSProxyCallSuite.class);

    public PureBCOSProxyCallSuite(
            String resourceOrAddress, String chainName, String accountName, boolean sm)
            throws Exception {
        super(chainName, accountName, sm, resourceOrAddress);

        logger.info(" ===>>> resourceOrAddress : {}", resourceOrAddress);
    }

    private ABIDefinition abiDefinition =
            getContractABIDefinition().getFunctions().get("get").get(0);
    private String methodId = abiDefinition.getMethodId();
    // private ABIObject inputObject = ABIObjectFactory.createInputObject(abiDefinition);
    // private String abi = getAbiCodecJsonWrapper().encode(inputObject, Arrays.asList()).encode();

    @Override
    public String getName() {
        return "BCOS Proxy Call Suite";
    }

    @Override
    public void call(PerformanceSuiteCallback callback) {
        try {
            if (getResource().isEmpty()) {
                callback.onFailed("Failed! status: Unsupported call by address");
            } else {
                byte[] sendBytes =
                        getWeCrossProxy()
                                .constantCall(getResource(), Numeric.hexStringToByteArray(methodId))
                                .send();

                if (logger.isTraceEnabled()) {
                    logger.trace(" result: " + Hex.toHexString(sendBytes));
                }

                if (sendBytes.length == 0) {
                    callback.onFailed("Failed! status: empty return");
                } else {
                    callback.onSuccess("Success");
                }
            }
        } catch (Exception e) {
            callback.onFailed("Failed! status: " + e.getMessage());
        }
    }
}
