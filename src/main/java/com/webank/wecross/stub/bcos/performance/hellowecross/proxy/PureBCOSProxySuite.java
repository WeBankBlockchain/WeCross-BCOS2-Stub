package com.webank.wecross.stub.bcos.performance.hellowecross.proxy;

import com.webank.wecross.stub.bcos.performance.hellowecross.HelloWeCross;
import com.webank.wecross.stub.bcos.performance.hellowecross.PureBCOSSuite;
import com.webank.wecross.stub.bcos.preparation.CnsService;
import com.webank.wecross.stub.bcos.web3j.Web3jWrapperImplV26;
import org.fisco.bcos.sdk.abi.wrapper.ABICodecJsonWrapper;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.abi.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public abstract class PureBCOSProxySuite extends PureBCOSSuite {

    private static Logger logger = LoggerFactory.getLogger(PureBCOSProxySuite.class);

    public PureBCOSProxySuite(
            String chainName, String accountName, boolean sm, String resourceOrAddress)
            throws Exception {
        super(chainName, accountName, sm);
        this.cnsInfo = CnsService.queryProxyCnsInfo(new Web3jWrapperImplV26(getWeb3j()));
        this.abiCodecJsonWrapper = new ABICodecJsonWrapper();
        this.contractABIDefinition = ABIDefinitionFactory.loadABI(HelloWeCross.ABI);
        if (Objects.nonNull(this.cnsInfo)) {
            this.weCrossProxy =
                    WeCrossProxy.load(
                            getCnsInfo().getAddress(),
                            getWeb3j(),
                            getCredentials(),
                            new StaticGasProvider(
                                    new BigInteger("30000000000"), new BigInteger("30000000000")));
            if (!resourceOrAddress.startsWith("0x")) { // address
                this.address = this.weCrossProxy.getAddressByNameByCache(resourceOrAddress).send();
                if (Objects.isNull(this.address)
                        || this.address.equals("0x0000000000000000000000000000000000000000")) {
                    System.err.println(" Resource: " + resourceOrAddress + " not exist.");
                    System.exit(0);
                }
                this.resource = resourceOrAddress;
                System.err.println(" ## Resource: " + resource + " ,address: " + address);
            } else { // resource
                this.resource = "";
                this.address = resourceOrAddress;
            }

            logger.info(" ===>> resource: {}, address: {}", resource, address);
        }
    }

    private CnsInfo cnsInfo;
    private ContractABIDefinition contractABIDefinition;
    private ABICodecJsonWrapper abiCodecJsonWrapper;
    private WeCrossProxy weCrossProxy;
    private String resource;
    private String address;

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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
