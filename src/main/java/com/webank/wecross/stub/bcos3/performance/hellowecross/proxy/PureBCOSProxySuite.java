package com.webank.wecross.stub.bcos3.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos3.performance.hellowecross.HelloWeCross;
import com.webank.wecross.stub.bcos3.performance.hellowecross.PureBCOSSuite;
import com.webank.wecross.stub.bcos3.preparation.BfsServiceWrapper;
import java.util.Objects;
import org.fisco.bcos.sdk.v3.codec.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.v3.codec.wrapper.ContractCodecJsonWrapper;
import org.fisco.bcos.sdk.v3.contract.precompiled.bfs.BFSInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PureBCOSProxySuite extends PureBCOSSuite {

    private static Logger logger = LoggerFactory.getLogger(PureBCOSProxySuite.class);

    private BFSInfo bfsInfo;
    private ContractABIDefinition contractABIDefinition;
    private ContractCodecJsonWrapper contractCodecJsonWrapper;
    private WeCrossProxy weCrossProxy;
    private String contractName;

    public PureBCOSProxySuite(
            String chainName, String accountName, boolean sm, String contractName) {
        super(chainName, accountName, sm);

        this.bfsInfo = BfsServiceWrapper.queryProxyBFSInfo(getAbstractClientWrapper());
        this.contractCodecJsonWrapper = new ContractCodecJsonWrapper();
        this.contractName = contractName;

        ABIDefinitionFactory abiDefinitionFactory = new ABIDefinitionFactory(getCryptoSuite());
        this.contractABIDefinition = abiDefinitionFactory.loadABI(HelloWeCross.ABI);
        if (Objects.nonNull(this.bfsInfo)) {
            this.weCrossProxy =
                    WeCrossProxy.load(getBfsInfo().getAddress(), getClient(), getCredentials());

            logger.info(" ===>> contractName: {}", contractName);
        }
    }

    public BFSInfo getBfsInfo() {
        return bfsInfo;
    }

    public void setBfsInfo(BFSInfo bfsInfo) {
        this.bfsInfo = bfsInfo;
    }

    public ContractABIDefinition getContractABIDefinition() {
        return contractABIDefinition;
    }

    public void setContractABIDefinition(ContractABIDefinition contractABIDefinition) {
        this.contractABIDefinition = contractABIDefinition;
    }

    public ContractCodecJsonWrapper getAbiCodecJsonWrapper() {
        return contractCodecJsonWrapper;
    }

    public void setAbiCodecJsonWrapper(ContractCodecJsonWrapper contractCodecJsonWrapper) {
        this.contractCodecJsonWrapper = contractCodecJsonWrapper;
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
