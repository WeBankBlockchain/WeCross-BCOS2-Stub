package com.webank.wecross.stub.bcos.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos.client.ClientWrapperImplV26;
import com.webank.wecross.stub.bcos.performance.hellowecross.HelloWeCross;
import com.webank.wecross.stub.bcos.performance.hellowecross.PureBCOSSuite;
import com.webank.wecross.stub.bcos.preparation.CnsService;
import java.util.Objects;
import org.fisco.bcos.sdk.abi.wrapper.ABICodecJsonWrapper;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.abi.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PureBCOSProxySuite extends PureBCOSSuite {

    private static Logger logger = LoggerFactory.getLogger(PureBCOSProxySuite.class);

    private CnsInfo cnsInfo;
    private ContractABIDefinition contractABIDefinition;
    private ABICodecJsonWrapper abiCodecJsonWrapper;
    private WeCrossProxy weCrossProxy;
    private String contractName;

    public PureBCOSProxySuite(String chainName, String accountName, boolean sm, String contractName)
            throws Exception {
        super(chainName, accountName, sm);
        this.cnsInfo = CnsService.queryProxyCnsInfo(new ClientWrapperImplV26(getClient()));
        this.abiCodecJsonWrapper = new ABICodecJsonWrapper();
        this.contractName = contractName;

        ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory(getCryptoSuite());
        this.contractABIDefinition = abiDefinitionFactory.loadABI(HelloWeCross.ABI);
        if (Objects.nonNull(this.cnsInfo)) {
            this.weCrossProxy =
                    WeCrossProxy.load(getCnsInfo().getAddress(), getClient(), getCredentials());

            logger.info(" ===>> contractName: {}", contractName);
        }
    }

    public CnsInfo getCnsInfo() {
        return cnsInfo;
    }

    public void setCnsInfo(CnsInfo cnsInfo) {
        this.cnsInfo = cnsInfo;
    }

    public ContractABIDefinition getContractABIDefinition() {
        return contractABIDefinition;
    }

    public void setContractABIDefinition(ContractABIDefinition contractABIDefinition) {
        this.contractABIDefinition = contractABIDefinition;
    }

    public ABICodecJsonWrapper getAbiCodecJsonWrapper() {
        return abiCodecJsonWrapper;
    }

    public void setAbiCodecJsonWrapper(ABICodecJsonWrapper abiCodecJsonWrapper) {
        this.abiCodecJsonWrapper = abiCodecJsonWrapper;
    }

    public WeCrossProxy getWeCrossProxy() {
        return weCrossProxy;
    }

    public void setWeCrossProxy(WeCrossProxy weCrossProxy) {
        this.weCrossProxy = weCrossProxy;
    }

    public String getContractName() {
        return contractName;
    }
}
